//---------------------------------------------------------------------------
// StateDMI_Processor - class to process StateDMI commands
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
//
// 2002-09-18	J. Thomas Sapienza, RTi	Initial version from TSTool and
//					PreCU code.
// 2002-09-19	JTS, RTi		Code to handle first query (do_read
//					CULocationsFromDDS)
// 2002-09-20	JTS, RTi		Removed getOutputFile methods.
// 2002-09-23	JTS, RTi		Added do_writeCULocations method.
// 2002-09-24	JTS, RTi		do_setCULocation finalized.
// 2002-09-25	JTS, RTi		Javadoc'd.
// 2002-09-30	JTS, RTi		Climate Weight code added.
// 2002-10-03	JTS, RTi		WES code added.
// 2002-10-04	JTS, RTi		Code for doing a query of the available
//					Blaney Criddle methods from HydroBase
//					was added.
// 2002-10-08	JTS, RTi		"AddIfNotFound" -> "AppendIfNotFound"
// 2002-10-09	JTS, RTi		fillCULocationFromHydroBase added, and
//					the readCULocationFrom* commands
//					changed to use it.
// 2002-10-10	JTS, RTi		CULocation objects being added to
//					__CULocations that match the ID of an
//					existing one in there are flagged
//					and a warning message displayed to the
//					user.
// 2002-10-22	Steven A. Malers, RTi	Review and update code for official
//					release.
//					* Make the object runnable and implement
//					  a run() method so that processing can
//					  be threaded in the GUI.
//					* To do so, simplify the processCommands
//					  logic to one version.
//					* Remove support for old command support
//					  (e.g., remove -d support with the
//					  debut level.  This decreases the size
//					  of the code tremendously.
//					* Start convention of printing to status
//					  level 2 from do_* methods to reiterate
//					  that a command has done what it is
//					  supposed to do.
//					* Start transition to DateTime from
//					  TSDate - use DateTime as much as
//					  possible, short of using in TS
//					  classes - hopefully will update TS to
//					  use DateTime soon.
//					* The working directory procedure was
//					  not quite right - update to be more
//					  robust.
//					* Remove formWDID and parseWDID that are
//					  in the HydroBase_WaterDistrct class.
//					* Alphabetize methods.
//					* Add finalize.
//					* Change names of main Vectors to have
//					  "Vector" in name.
//					* Change so when writing files, indicate
//					  the type of the file so that multiple
//					  write commands can be implemented
//					  (e.g., use writeCULocationsToSTR).
// 2002-11-23	Steven A. Malers, RTi	Add features for 01.01.00:
//					* Add support for StateCU .cch file.
// 2002-02-17	SAM, RTi		Finalize for first official release:
//					* Add getOutputPeriodStart() and
//					  getOutputPeriodEnd() for use in the
//					  main GUI.
//					* Finish .cch support.
//					* Finish .wts support.
//					* Finish .kbc support.
//					* Finish "initial" .cds support.
// 2003-03-06	SAM, RTi		* Add a flag indicating how wide WDIDs
//					  should be to allow default to be 7 but
//					  default to what appears to be in the
//					  data.
//					* When processing wildcards in this
//					  code, replace GUI "*" with
//					  Java-required ".*" before calling
//					  String.matches().
//					* Remove all unused code from old
//					  StateDMI - things have evolved to
//					  where that code will likely not be
//					  used.
// 2003-05-11	SAM, RTi		* Update to to new StateCU file
//					  conventions.
//					* Change all commands to not use file
//					  extensions in read/write commands but
//					  instead just use the model name.
// 2003-06-04	SAM, RTi		* Change name of class from
//					  StateDMIProcessor to
//					  StateDMI_Processor.
//					* Update to new StateCU_XXX class names.
//					* Remove station weights file.
//					* Use new TS package (DateTime, etc.).
// 2003-10-15	SAM, RTi		* Comment out StateCU_ParameterTS - need
//					  to change to
//					  StateCU_IrrigationPracticeTS.
//					* Start re-enabling all the StateCU
//					  commands.
// 2004-02-25	SAM, RTi		* Enable fillCULocation() and
//					  setCULocation().
//					* Enable readCULocationsFromList().
//					* Enable readCULocationsFromStateCU().
// 2004-02-26	SAM, RTi		* Change setCULocationAggregate() to
//					  setDiversionAggregate().
//					* Add setDiversionSystem().
//					* Add setWellAggregate().
//					* Add setWellSystem().
// 2004-02-28	SAM, RTi		* Enable readClimateStationsFromList().
// 					* Enable writeClimateStationsToStateCU()
//					* Add fillClimateStation().
//					* Add setClimateStation().
//					* Add fillClimateStationsFromHydroBase()
//					* Updated because some utility code
//					  moved from StateCU_data to
//					  StateCU_Util.
// 2004-02-29	SAM, RTi		* Add
//					  createCropPatternTSForCULocations().
//					* Update
//					  writeCropPatternTSToDateValue().
//					* Update writeCropPatternTSToStateCU().
//					* Enable readCropPatternTSFromStateCU().
// 2004-03-01	SAM, RTi		* Enable
//					  readCropPatternTSFromHydroBase().
//					* Enable fillCropPatternTSInterpolate().
//					* Enable fillCropPatternTSRepeat().
//					* Add fillCropPatternTSProrateAgStats().
//					* Change StateCU_Location "aggregate"
//					  notation to "collection so as to not
//					  confuse with the true aggregate type.
//					* For well aggregate/system, require a
//					  division to be set because it is
//					  needed to uniquely identify parcels.
//					* Add AgStats time series to the data
//					  that are internally maintained, as
//					  needed to fill time series data.
// 2004-03-02	SAM, RTi		* Add setCropPattern().
//					* Add translateCropPatternTS().
// 2004-03-03	SAM, RTi		* Add readCropPatternTSFromDBF().
// 					* Add removeCropPatternTS().
// 					* Add createIrrigationPracticeTSFor
//					  CULocations().
// 					* Add
//					  writeIrrigationPracticeTSToStateCU().
// 					* Add writeIrrigationPracticeTS
//					  ToDateValue().
//					* Enable
//					  readIrrigationPracticeTSFromStateCU().
//					* Change setCropPattern() to
//					  setCropPatternTS().
// 2004-03-07	SAM, RTi		* Update do_readCropPatternTSFromDBF()
//					  to support reading sprinkler parcels.
// 2004-03-09	SAM, RTi		* Combine set*Aggregate(), etc, into
//					  setCollection().
//					* Add setCollectionFromList() to handle
//					  set*FromList() aggregate/system
//					  commands.
// 2004-03-12	SAM, RTi		* Add
//					  do_readIrrigationPracticeWellData() to
//					  handle readIrrigationPracticeWellData
//				 	  FromList() and later fromHydroBase().
//					* Change do_readCropPatternTSFromDBF()
//					  to do_readCropParcels() to more
//					  accurately reflect its multiple use.
// 2004-03-13	SAM, RTi		* Enable the Cancel button to in the
//					  warning dialog when processing
//					  commands - it is inconvenient to
//					  cancel using the other menus.
//					* Change __cancel to __cancel_processing
//					  to be clearer and consistent with
//					  TSTool.
//					* When there is not a parcel ID in the
//					  sprinkler file - print a status
//					  message but don't treat as a warning.
// 2004-03-14	SAM, RTi		* When processing well data for IPY,
//					  check for parcel data and print a
//					  warning if it is not found.
//					* Enable all crop characteristics
//					  commands using current standards.
// 2004-03-17	SAM, RTi		* Enable all Blaney-Criddle commands
//					  using current standards.
//					* Enable delay table processing commands
//					  for StateCU.
// 2004-03-18	SAM, RTi		* Enable delay table assignment
//					  commands.
// 2004-03-22	SAM, RTi		* Enable irrigation practice time series
//					  fill commands.
// 2004-04-02	SAM, RTi		* Add fillCULocationsFromList() and
//					  setCULocationsFromList().
// 2004-04-04	SAM, RTi		* Finalize previous item.
//					  Enable
//					  setCULocationClimateStationWeights().
//					* Fix bug in setCropPatternTS() where
//					  pattern tokens are not being handled
//					  properly.
// 2004-04-05	SAM, RTi		* Add
//					  setIrrigationPracticeTSFromStateCU().
//					* Add ID parameter to
//					  setIrrigationPracticeTSFromList().
// 2004-04-10	SAM, RTi		Update to version 01.10.00.
//					* For write*() commans, skip of no
//					  output is desired.
//					* Add __create_output data member and
//					  set method and update
//					  processCommands() to check it.
// 2004-04-12	SAM, RTi		* Add
//					  readDiversionStationsFromNetwork().
//					* Add
//					  writeDiversionsStatinosToStateMod().
// 2004-05-11	SAM, RTi		* Fix so that setCropPatternTS() sets
//					  to zero any crops not specifically
//					  referenced in the command - this
//					  prevents unforseen filling problems
//					  with values set to missing.
// 2004-05-17	SAM, RTi		* Similar to above for
//					  readCropPatternTSFromHydroBase(),
//					  readCropParcels().
//					* Change so that only the "NA"
//					  irrig_type is filtered out when
//					  processing parcels.  An irrig_type of
//					  "UNKNOWN" indicates that a parcel is
//					  irrigated but the irrigation type is
//					  unknown.  A type of "NA" indicates
//					  that the parcel is not irrigated.
//					* Update setCropPatternTS() to use the
//					  ProcessWhen flag, which sets aside
//					  supplemental crop pattern data to be
//					  processed with other commands.
//					* Update
//					  readCropPatternTSFromHydroBase() to
//					  consider the supplemental crop pattern
//					  data.
// 2004-05-25	SAM, RTi		* Add
//					  fillCULocationClimateStationWeights().
//					* Update translateCropPatternTS() to
//					  accept an optional list file.
//					* Fix bug in
//					  setIrrigationPracticeTSFromList() - it
//					  did not seem to be doing anything.
//					* Comment out calls to removeAllTS(),
//					  which were in place for crop pattern
//					  time series.  These apparently were
//					  in place when reading from HydroBase
//					  and got propagated to the DBF
//					  processor.  The problem is that if
//					  data are superimposed from different
//					  sources, the logic would clear out
//					  some data.  Comment out for now.
// 2004-05-28	SAM, RTi		* Add fillCropPatternTSConstant().
// 2004-06-01	SAM, RTi		* Start cranking out StateMod commands.
// 2004-06-13	SAM, RTi		Continue with StateMod file support.
//					* Distinguish between monthly and daily
//					  delay tables.
// 2004-06-17	SAM, RTi		Continue with StateMod file support.
//					* Add diversion rights commands.
// 2004-06-24	SAM, RTi		Continue with StateMod file support.
//					* Add network commands.
//					* Fix bug in do_setCollectionFromList -
//					  if the maximum column was specified,
//					  it was getting reset to zero.
// 2004-06-30	SAM, RTi		* Add HydroBase version comments to all
//					  write commands.
// 2004-07-06	SAM, RTi		Continue with StateMod file support.
//					* Add instream flow stations commands.
//					* Add instream flow demand commands.
// 2004-07-08	SAM, RTi		Continue with StateMod file support.
//					* Add instream flow rights commands.
// 2004-07-09	SAM, RTi		Continue with StateMod file support.
//					* Finish instream flow demand commands.
// 2004-07-10	SAM, RTi		Continue with StateMod file support.
//					* For read*FromStateMod(), allow only
//					  a filename (old syntax) or new
//					  InputFile="X" parameter.
//					* Similar for StateCU - will need to
//					  update dialogs as time allows.
// 2004-07-11	SAM, RTi		Continue with StateMod file support.
//					* Finalize RIS commands.
//					* Finalize SES commands.
//					* Finalize RIN commands.
// 2004-08-12	SAM, RTi		Continue with StateMod file support.
//					* Enable RIB commands.
//					* Fix bug where diversion station
//					  default return was not ignoring
//					  confluences.
//					* For diversion stations, check the
//					  output year type so that the
//					  efficiencies can be properly assigned.
//					* Add setOutputYearType() command.
// 2004-08-22	SAM, RTi		Continue with StateMod file support.
//					* Finalize DDH commands except for
//					  limit*toRights() command.
// 2004-08-23	SAM, RTi		Continue with StateMod file support.
//					* Finalize DDM commands.
// 2004-08-29	SAM, RTi		Continue with StateMod file support.
//					* Finalize DDM commands (still).
// 2004-08-30	SAM, RTi		Continue with StateMod file support.
//					* Add
//					  setDiversionStationCapacitiesFromTS()
//					  for diversion historical TS.
// 2004-09-01	SAM, RTi		Continue with StateMod file support.
//					* Enable setDiversionDemandTSMonthly.
//					* Add the efficiency detail report to
//					  calculateDiversionStation
//					  Efficiencies().
//					* Add fill commands for diversion
//					  demand time series (monthly).
//					* Fix limitation that fill start and end
//					  were only handled as years.
// 2004-09-09	SAM, RTi		Continue with StateMod file support.
//					* Finalize reservoir station commands.
// 2004-09-14	SAM, RTi		Continue with StateMod file support.
//					* Finalize reservoir rights commands.
// 2004-09-16	SAM, RTi		Continue with StateMod file support.
//					* Finalize well station commands.
// 2004-09-20	SAM, RTi		Continue with StateMod file support.
//					* More finalize well station commands.
// 2004-09-29	SAM, RTi		Continue with StateMod file support.
//					* Finalize well rights commands.
//					* Finalize well demands commands.
// 2004-09-30	SAM, RTi		Continue with StateMod file support.
//					* Continue finalizing well demands
//					  commands.
// 2004-10-04	SAM, RTi		* Keep track of output files so that
//					  they can be listed in the Results
//					  section of the GUI.
// 2004-10-20	SAM, RTi		* Fix bug in readSupplementalStructure
//					  IrrigSummaryTSList - a loop was using
//					  the wrong max and this was throwing
//					  exceptions.
// 2004-11-01	SAM, RTi		Work with Erin Wilson to recreate the
//					Rio Grande files.
//					* Add the readIrrigationPracticeWellData
//					  FromHydroBase() command.
//					* StateMod_ReservoirStation.setN2owns()
//					  has been removed - use setN2own().
// 2004-12-15	SAM, RTi		* Filling diversions from HydroBase was
//					  crashing for the San Juan.  Add
//					  another try/catch to allow processing
//					  to continue.
// 2005-01-12	SAM, RTi		* The name in a collection list file was
//					  not being set in the station.
// 2005-01-13	SAM, RTi		Update to 1.16.03:
//					* Change the default value for WriteHow
//					  to "OverwriteFile".  This seems to
//					  make more sense for must cases.
//					* The output year type was not getting
//					  saved to the global variable - fix.
//					* Add setDiversionHistoricalTS
//					  MonthlyConstant().
// 2005-01-18	JTS, RTi		* Added getSMInstreamFlows().
//					* Added getSMInstreamFlowRights().
//					* Added getSMReservoirRights().
//					* Added getSMStreamEstimates().
//					* Added
//					  getSMStreamEstimateCoefficients().
//					* Added getSMWellRights().
//		SAM, RTi		* Rename the getSM*() methods to more
//					  verbose names that match the StateMod
//					  and StateCU class names.
// 2005-01-20	SAM, RTi		* Combine setTSConstantMonthly() and
//					  setTSConstant() - the code is used by
//					  a limited number of commands so
//					  combine now to allow for future
//					  growth.
//					* Fix so that set*TimeSeries() will
//					  read from HydroBase.
// 2005-01-25	SAM, RTi		* Add sortDiversionHistoricalTSMonthly()
//					  command.
// 2005-01-25	SAM, RTi		Update to version 01.17.01.
//					* Fix bug where when adding a blank
//					  diversion historical time series from
//					  HydroBase the allocateDataSpace()
//					  method was not getting called.
//					* Null time series limits was causing
//					  an error filling with monthly average.
// 2005-01-27	SAM, RTi		Update to version 01.17.02.
//					* Simplify
//					  readSprinklerParcelsFromList() to rely
//					  on HydroBase data for parcel data.
//					  The list file only provides a list of
//					  parcels that have sprinklers.
// 2005-01-31	SAM, RTi		Update to version 01.17.03.
//					* Continue the above.
//					* Fix bug where processCommands() had
//					  two main if statements - this was
//					  leading to some spurious warnings.
//					* Instream flow demand (average monthly)
//					  was checking output period - instead
//					  change to use output year type.
//					* Change so when reading stations from
//					  the network the river node is
//					  automatically defaulted to the node
//					  ID.
// 2005-02-01	SAM, RTi		Update to version 01.17.04.
//					* Writing reservoir rights was mangling
//					  some of the dead storage and acw
//					  information, when dead storage is > 0.
//					  Fix to write out simple values with
//					  no adjustments for dead storage.
//					* Fix bug where fill/set of rights data
//					  using a StationID of "ID" was using
//					  the literal "ID" instead of the first
//					  part of the identifier.
// 2005-02-03	SAM, RTi		Update to version 01.17.05.
//					* Fix limitDiversionHistoricalTS
//					  MonthlyToRights(). A backup copy of
//					  data was not being saved and
//					  appropriation dates were not being
//					  reinitialized for each station.
//					* Fix
//					  calculateDiversionDemandTSMonthly() to
//					  set the demand to zero if the
//					  efficiency is zero and IWR is zero.
//					  Previously demand would be missing.
//					* Update fillDiversionHistoricalTS
//					  MonthlyAverage() and fillDiversion
//					  HistoricalTSMonthlyPatter() to have
//					  the IncludeCollections parameter.
//					* Update readDiversionHistoricalTS
//					  MonthlyFromHydroBase() to do filling
//					  of aggregate parts during the read.
// 2005-02-03	SAM, RTi		Update to version 01.17.06.
//					* Add the efficiency report to the list
//					  of output files.
//					* The list if station IDs to ignore was
//					  not being handled properly.
// 2005-02-09	SAM, RTi		Update to version 01.17.07.
//					* Add limitDiversionDemandTSMonthly
//					  ToRights().
// 2005-02-10	SAM, RTi		Update to version 01.17.08.
//					* Handle the water right switch when
//					  limiting diversions and demands to
//					  rights.
// 2005-02-14	SAM, RTi		Update to version 01.17.09.
//					* Resolve sort order for streamflow
//					  stations.  Was getting stream gages
//					  first and then other nodes.  Make the
//					  default the same order as the network.
// 2005-02-25	SAM, RTi		Update to version 01.17.10.
//					* Add synchronizeIrrigationPracticeAnd
//					  CropPatternTS.
//					* Update readCropPatternTSFromHydroBase
//					  to have a ProcessData parameter so
//					  that data can be used when processing
//					  the irrigation practice time series.
// 2005-02-27	SAM, RTi		* Copy the read readCropParcels()
//					  command to readIrrigationPracticeTS()
//					  and update to handle logic for the
//					  sprinkler acres, groundwater acres,
//					  and maximum pupmping.
//					* Change readIrrigationPracticeWellData
//					  FromList to.
//					  readIrrigationPracticeTSWellData
//					  FromList to be more consistent.
//					* Update the readWellRightsFromHydroBase
//					  to have DefaultAppropriationDate and
//					  DefineRightHow parameters.
//					* Update the readWellRightsFromHydroBase
//					  command to process the groundwater and
//					  sprinkler are a and maximum pumping.
//					* Add the sortCULocations() command.
// 2005-03-10	SAM			* Add OnOffDefault for the commands that
//					  read water rights from HydroBase.
//					* When aggregating rights, make sure
//					  that the aggregate result is a whole
//					  number to accurately reflect the
//					  appropriation dates that go into the
//					  averaging.  This was not being handled
//					  properly in the well rights processing
//					  code.  Remove code that would allow a
//					  simple numeric weighting.
//					* Modify findAndAddSMInstreamFlowRight()
//					  command to insert new rights
//					  alphabetically.  The other rights were
//					  being handled this way.
//					* Add sort*Right() commands so that the
//					  user has full flexibility to make the
//					  rights agree after set commands.
//					* When processing diversion and well
//					  rights, change to allow units of "C"
//					  and "CFS" - is the latter being phased
//					  into HydroBase?
//					* Begin phasing in the new message tags
//					  to facilitate user review of the log
//					  file.
// 2005-03-21	SAM, RTi		Update to version 1.17.11.
//					* Focus on getting the IPY code working.
//					* Enable the setIrrigationPracticeTS
//					  FromHydroBase() command.
// 2005-03-23	SAM, RTi		* Start being more conscious about using
//					  "warning_count" instead of
//					  "error_count" and expand the message
//					  tag for some commands to include the
//					  warning count.
//					* Enable setIrrigationPracticeTS
//					  SprinklerAreaFromList().
// 2005-03-25	SAM, RTi		Update to version 1.17.12.
//					* Add warnings for obsolete commands
//					  to facilitate transition to StateDMI.
// 2005-03-30	SAM, RTi		* Change "there were # errors processing
//					  the command" to "there...warnings...".
// 2005-04-14	JTS, RTi		Added code to process write*ToList()
//					commands.
// 2005-04-18	JTS, RTi		* Added the code to run the
//					  readReservoirRightsFromStateMod()
//					  command.
//					* Added the code to run the
//					  readWellRightsFromStateMod() command.
//					* Added the code to run the
//				    	  readStreamEstimateCoefficients
//					  FromStateMod()
//					  command.
// 2005-04-19	JTS, RTi	 	* Added the code to run the
//					  readDelayTablesFromStateCU() command.
//					* Added the code to run the
//					  readCULocationDelayTableAssignments
//					  FromStateCU() command.
// 2005-05-02	SAM, RTi		* Review commands as the Rio Grande
//					  data set creation verification is
//					  performed.  Adhere to new guidelines
//					  for the message levels.
//					* Introduce __FYI_warning_level to
//					  facilitate handling of non-fatal
//					  warnings.
//					* Review and Javadoc writeToList() code.
//					* Test usiing stored procedures to
//					  regenerate the Rio Grande data set:
//					  Update
//					  fillClimateStationsFromHydroBase to
//					  query each station separately.
// 2005-05-20	SAM, RTi		* Phase in new Command classes
//					  consistent with TSTool.
//					* Restore some code from the previous
//					  version - mystery as to why it was
//					  deleted!?
//					* Add sorting of Blaney-Criddle data.
//					* Add Precision property to
//					  writeBlaneyCriddleToStateCU.
//					* Convert from using
//					  readStructureGeolocForWDID() to
//					  readStructureViewForWDID().
//					* Convert from using
//					  readStructureForWDID() to
//					  readStructureViewForWDID().
//					* Convert
//					  readStructureReservoirListForWDID() to
//					  readStructureReservoirForWDID().
//					* Convert readNetAmtsList() to the new
//					  calling sequence.
//					* Convert readStructureIrrigSummaryTS
//					  ListForWDIDListLand_usePeriod() to
//					  readStructureIrrigSummaryTSList().
//					* Update
//					  readParcelUseTSListForParcelList()
//					  and readParcelUseTSList()
//					  calls to not pass the where and order
//					  by clauses - null were being passed
//					  before anyhow.
// 2005-05-24	SAM, RTi		* Add Version parameter to
//					  writeCULocationsToStateCU().
//					* Convert all commands to use new
//					  command tag features - this should
//					  allow the log viewer to have full
//					  capabilities.  Fine-tuning the
//					  messages must still occur.
//					* Remove HydroBase_StationGeoloc and
//					  instead use HydroBase_StationView.
//					* Remove
//					  HydroBase_StructureIrrigSummaryTS and
//					  use HydroBase_StructureView.
// 2005-05-30	SAM, RTi		* Update setIrrigationPracticeTS
//					  SprinklerAreaFromList() to include
//					  the ParcelAreaCol parameter.
//					* Fix bug where sortReservoirStations()
//					  was not being recognized.
//					* Update remaining readNetAmtsList() to
//					  use new calling convention.
// 2005-06-03	SAM, RTi		Update to version 01.17.14.
//					* Add WriteOnlyTotal to
//					  writeCropPatternTSToStateCU().
//					* Update writeCropPatternTSToStateCU to
//					  use the output period.
// 2005-06-08	SAM, RTi		* Add Version parameter to
//					  readCropPatternTSFromStateCU().
//					* Add openHydroBase() command.
//					* Add Version parameter to
//					  readIrrigationPracticeTSFromStateCU().
//					* Add ReadStart, ReadEnd to
//					  readDiversionHistoricalTS*
//					  FromHydroBase().
// 2005-06-09	SAM, RTi		* Fix bug where reading diversion
//					  historical time series was
//					  initializing to the first part and
//					  adding the first part again.
//					* Enable flags for filling diversions
//					  with historical average, pattern,
//					  constant, and limiting to rights.
// 2005-06-29	SAM, RTi		* Add DefineRightHow=LatestDate when
//					  processing well rights.
// 2005-07-06	SAM, RTi		* Fix problem in setCropPatternTS()
//					  where the results were not getting
//					  refreshed after the initial
//					  processing, resulting in zeros in the
//					  output.
//					* Clarify message when pattern file is
//					  not found.
// 2005-07-13	SAM, RTi		Update to version 01.17.17.
//					* Update readCropPatternTS
//					  FromHydroBase() to truncate the
//					  parcel acreage to .2, to compare with
//					  work done by Leonard Rice.
// 2005-07-16	SAM, RTi		* Update
//					  setIrrigationPracticeTSFromList() so
//					  that other than the efficiencies can
//					  be set.
// 2005-07-27	SAM, RTi		Update to version 01.17.18.
//					* Fix bug in
//					  writeCropPatternTSToStateCU(), where
//					  the WriteCropArea parameter was not
//					  defaulting properly.
//					* Implement new parameters to allow the
//					  user to control how synchronize
//					  IrrigationPracticeAndCropPatternTS()
//					  executes.
// 2005-07-28	SAM, RTi		* Add check for sprinkler > groundwater
//					  area in synchronize.
// 2005-07-30	SAM, RTi		* Update setIrrigationPracticeTS
//					  MaxPumpingToRights() to have
//					  NumberOfDaysInMonth parameter.
// 2005-07-31	SAM, RTi		* Update
//					  fillCropPatternTSProrateAgStats() to
//					  have the NormalizeTotals parameter.
// 2005-08-11	SAM, RTi		Update to version 01.17.19.
//					* Fix bug in reading the AgStats
//					  DateValue file - was going into an
//					  infinite loop of warnings if no file
//					  was found.
//					* Add IgnoreUseType in
//					  readDiversionRightsFromHydroBase.
//					* Fix so that if an aggregate/system
//					  diversion part has missing capacity,
//					  the total capacity is not incremented
//					  for the part.
//					* Fix so that the total diversion time
//					  series from HydroBase is properly
//					  initialized even if the first
//					  diversion in the collection does not
//					  have a time series in HydroBase.
//					* Add a checkDataSet() method that is
//					  called after processing commands, to
//					  verify the integrity of the data set
//					  components.  Implement checks that
//					  were in place in watright and demandts
//					  (e.g., make sure that a location ID
//					  is only one type).
// 2005-08-18	SAM, RTi		Update to version 01.17.20.
//					* For well-only aggregates, do not put a
//					  "W" in the water right ID.  D&W still
//					  has the "W", as per Watright.
//					* Add a check for the existance of the
//					  delay table file.
//					* Fix bug where last year filling crop
//					  pattern time series by proration was
//					  not getting normalized to basin
//					  statistics.
// 2005-09-08	SAM, RTi		* Fix bug in setCropPatternTS() where
//					  overriding an existing pattern causes
//					  problems.  Clearing the crops was
//					  using the crop types in the set, not
//					  in the original time series.
//					* When using a time series from an
//					  external file, reset the period to
//					  the output period so that filling
//					  works.
//					* When setting a time series to a
//					  constant, the set end date was not
//					  being parsed properly (start was being
//					  used again).  Also, the year was being
//					  set to zero in call cases, when it
//					  should have been set to zero only for
//					  instream flow average monthly demand.
//					* When filling diversion time series
//					  with diversion comments, read the
//					  comments after setting the period of
//					  record.
// 2005-09-27	SAM, RTi		Update to version 01.17.21.
//					* Update to include the "writeToList"
//					  output files in the list of files that
//					  can be viewed.
//					* Fix so efficiency report is added to
//					  the output list using a relative path.
// 2005-10-03	SAM, RTi		Update to version 01.18.00.
//					* Begin finalizing list commands and do
//					  an official release.
//					* Enable well historical pumping time
//					  series commands, very similar to
//					  diversion historical time series.
// 2005-10-18	SAM, RTi		Update to version 1.18.04.
//					* Use "NA" instead of "N/A" to indicate
//					  that a well station is not tied to a
//					  diversion station.
//					* When processing well demand time
//					  series either to calculate average
//					  efficiencies or to calculate demand
//					  time series (using the average
//					  efficiencies), only process well
//					  stations where idivcomw=1.
// 2005-11-14	SAM, RTi		Update to version 1.18.05.
//					* Update fillCULocation() and
//					  setCULocation() to include elevation
//					  and AWC.
//					* Add setDiversionStationsFromList().
//					* Add __OutputYearStartMonth to
//					  facilitate conversions between year
//					  types.
//					* Update the StateCU CDS code to include
//					  the start/end months and year type in
//					  the file header.
//					* Update the StateCU IPY code to include
//					  the start/end months, units, and year
//					  type in the file header.
//					* Add the mergeListFileColumns()
//					  command.
// 2005-11-14	SAM, RTi		Update to version 1.18.06.
//					* Add readDiversionDemandTSMonthlyFrom
//					  StateMod.
// 2005-11-22	SAM, RTi		Update to version 1.18.07.
//					* Add AWC and elevation to
//					  readCULocationsFromList().
//					* Convert mergeListFileColumns() to a
//					  command class.
// 2005-12-01	SAM, RTi		Fix fillCropPatternTSProrateAgStats() so
//					that all county crops are used even if
//					a structure does not have a crop type.
// 2005-12-06	SAM, RTi		Update to version 1.18.08:
//					* Add
//					  fillDiversionStationsFromNetwork(),
//					  fillInstreamFlowStationsFromNetwork(),
//					  fillReservoirStationsFromNetwork(),
//					  fillWellStationsFromNetwork().
// 2006-01-11	SAM, RTi		Update to version 1.18.09:
//					* Fix bug where space in setWorkingDir()
//					  caused an exception.  The diretory
//					  with a space was not being quoted in
//					  the StateDMI_JFrame class.
// 2006-01-30	SAM, RTi		Update to version 1.18.10:
//					* Add IgnoreWells and IgnoreDWs to the
//					  readWellDemandTSMonthlyFromStateMod()
//					  command.
//					* Apparently the above were not enabled
//					  for readWellStationsFromStateMod(),
//					  even though documented.  Enable.
// 2006-01-31	SAM, RTi		Update to version 1.19.00:
//					* Change so that well station types are
//					  used for D&W aggregate lists when
//					  processing wells.  This impacts:
//					  setWellAggregate()
//					  setWellAggregateFromList()
//					  setWellSystem()
//					  setWellSystemFromList()
// 2006-03-10	SAM, RTi		* Continue to work on the above changes,
//					  testing also with South Platte data.
// 2006-04-10	SAM, RTi		Update to version 1.20.00:
//					* Fix bug where reading collection list
//					  file did not warn when specified
//					  column numbers were greater than the
//					  number of columns in the file.
//					* When processing commands, first create
//					  StateMod and StateCU data sets, so
//					  that information can be recorded about
//					  components that are output, and store
//					  data check information for the
//					  components.
// 2006-04-17	SAM, RTi		* Add IDFormat to
//					  readWellRightsFromHydroBase, to allow
//					  some control over well right
//					  identifiers.
//					* For sort*(), change the default from
//					  "Alphabetical" to "Ascending".
//					* In readWellRightsFromHydroBase(),
//					  save the parcel counts so that data
//					  checks can be done.
// 2006-04-30	SAM, RTi		Update to version 01.20.02.
//					* Write the check file even if no check
//					  messages were generated.
//					* Add translateCropCharacteristics().
//					* Add translateBlaneyCriddle().
//					* Set well right identifiers to the
//					  receipt number when processing
//					  permits.
// 2006-07-07	SAM, RTi		Update to version 01.20.05.
//					* In synchronizeIrrigationPracticeAnd
//					  CropPatternTS(), remove the code that
//					  resets groundwater acreage to
//					  sprinkler acreage if groundwater is
//					  less - it is unneeded.
// 2006-10-09	SAM, RTi		Update to version 1.21.00:
//					* Adjust reading well rights to reread
//					  from the database rather than relying
//					  on the "wells" table and add the APEX
//					  amounts to the absolute decrees.
// 2006-10-24	SAM, RTi		Update to version 1.22.00:
//					* Fix problem with well right IDs not
//					  always being set for cases from
//					  the previous update.
// 2006-11-03	SAM, RTi		Update to version 2.00.00:
//					* Fix problem with
//					  setStreamGageStation() not setting the
//					  name.
// 2006-12-18	SAM, RTi		* Fix problem with
//					  readDiversionRightsFromHydroBase()
//					  IgnoreUseType not working properly.
// 2007-01-03	Kurt Tometich, RTi
//						Added the processing capability
//					  	for a new command setRiverNetworkNode.
//					  	There is a new method do_setRiverNetworkNode
//					  	Which handles the processing.  This is a
//					  	StateMod specific command.
// 2007-01-07	KAT, RTi	Added validation for numerical fields
// 						for setRiverNetworkNode processing.
// 2007-01-10 	KAT, RTi 	Added support for new command
//						"sortCropCharacteristics."  Added support
//						for new field "Blaney-Criddle Method" in
//						the do_setBlaneyCriddle() method.
// 2007-02-05	KAT, RTi	Moved the
//						do_readDiversionHistoricalTSMonthlyFromHydroBase
//						method to a separate command class and dialog.
//						Removed old functionality from processCommands.
// 2007-02-06	KAT, RTi 	Changed all protected vectors to private
//						and added some getMethods().
// 2007-02-18	SAM, RTi	Update to new CommandProcessor interface.
//					Clean up code based on Eclipse feedback.
//					Update translateCropPatternTS() to include ID.
// 2007-03-04	SAM, RTi	Add ktsw defaults for Blaney-Criddle processing.
// 2007-03-22	SAM, RTi	Change setIrrigationPracticeTSFromHydroBase() to
//					only process crop years in the output period.
// 2007-05-11	SAM, RTi	Update CDS filling methods to filter by GW-only
//					and surface supply locations.
//------------------------------------------------------------------------------
// EndHeader

package DWR.DMI.StateDMI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.lang.String;
import java.lang.StringBuffer;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.TS.DateValueTS;
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.MonthTSLimits;
import RTi.TS.StringMonthTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSSupplier;
import RTi.TS.TSUtil;
import RTi.TS.YearTS;

import RTi.Util.GUI.ResponseJDialog; // TODO SAM 2006-05-15 minimize when batch mode is enabled.
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandListListener;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorEvent;
import RTi.Util.IO.CommandProcessorEventListener;
import RTi.Util.IO.CommandProcessorEventProvider;
import RTi.Util.IO.CommandProcessorListener;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandProfile;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusProviderUtil;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatusUtil;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.GenericCommand;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessListener;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.RequestParameterNotFoundException;
import RTi.Util.IO.UnknownCommandException;
import RTi.Util.IO.UnrecognizedRequestException;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageJDialog;
import RTi.Util.Message.MessageJDialogListener;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.StopWatch;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

import rti.tscommandprocessor.commands.check.CheckFileCommandProcessorEventListener;
import rti.tscommandprocessor.commands.util.Comment_Command;
import rti.tscommandprocessor.commands.util.CommentBlockStart_Command;
import rti.tscommandprocessor.commands.util.CommentBlockEnd_Command;
import rti.tscommandprocessor.commands.util.Exit_Command;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_CUMethod;
import DWR.DMI.HydroBaseDMI.HydroBase_CUPenmanMonteith;
import DWR.DMI.HydroBaseDMI.HydroBase_CountyRef;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import DWR.DMI.HydroBaseDMI.HydroBase_Structure;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureToParcel;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;

import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_DelayTable;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_InstreamFlowRight;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_OperationalRight;
import DWR.StateMod.StateMod_Plan;
import DWR.StateMod.StateMod_Plan_WellAugmentation;
import DWR.StateMod.StateMod_PrfGageData;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_ReturnFlow;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamEstimate_Coefficients;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;

import DWR.StateCU.StateCU_BlaneyCriddle;
import DWR.StateCU.StateCU_ClimateStation;
import DWR.StateCU.StateCU_CropCharacteristics;
import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_DataSet;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_PenmanMonteith;
import DWR.StateCU.StateCU_Util;

/**
Class for processing StateDMI commands.
*/
public class StateDMI_Processor
implements CommandProcessor, CommandProcessorEventListener, MessageJDialogListener, TSSupplier
{

private final int __FYI_warning_level = StateDMI_Util._FYI_warning_level;

/**
The GUI to which this StateDMI_Processor may be attached, which is used to retrieve the
model network object.
*/
private StateDMI_JFrame __gui;

/**
The list of commands managed by this command processor, guaranteed to be non-null.
*/
private List __commandList = new Vector();

/**
The name of the file to read for commands.
*/
private String __commandFilename = null;

/**
The array of CommandListListeners to be called when the command list changes.
*/
private CommandListListener [] __CommandListListener_array = null;

/**
The array of CommandProcessorListeners to be called when the commands are
running, to indicate progress.
*/
private CommandProcessorListener [] __CommandProcessorListener_array = null;

/**
The list of CommandProcessorEventListener managed by this command processor,
which is currently used only by the check file.  See the
OpenCheckFile command for creation of the instances.
*/
private CommandProcessorEventListener[] __CommandProcessorEventListener_array = null;

/**
If true, all commands that create output, including write*(), will be
processed.  If false, they will not.  The latter results in faster
processing and only in-memory results.
*/
private boolean __create_output = true;

/**
The initial working directory for processing, typically the location of the commands
file from read/write.  This is used to adjust the working directory with
SetWorkingDir() commands and is used as the starting location with RunCommands().
*/
private String __InitialWorkingDir_String;

/**
The current working directory for processing, which is determined from the initial
working directory and adjustments from setWorkingDir() commands.
*/
private String __WorkingDir_String;

/**
Hashtable of properties used by the processor.
*/
Hashtable __property_Hashtable = new Hashtable();

/**
Indicates whether the processCommands() is currently running.
*/
private volatile boolean __is_running = false;

/**
Indicates whether the processing loop should be canceled.  This is a request
(e.g., from a GUI) that needs to be handled as soon as possible during command
processing.  It is envisioned that cancel can always occur between commands and
as time allows it will also be enabled within a command.
*/
private volatile boolean __cancel_processing_requested = false;

/**
Special flag to handle limitDiversionHistoricalTSMonthlyToRights() command,
which requires that a copy of the original data are saved.
*/
protected boolean __need_diversion_ts_monthly_copy = false;

/**
The first date of the output period.
*/
private DateTime __OutputStart_DateTime = null;

/**
The last date of the output period.
*/
private DateTime __OutputEnd_DateTime = null;

/**
The output year type ("Calendar", "Water", or "NovToOct").
*/
private YearType __OutputYearType = YearType.CALENDAR; // Default

// TODO SAM 2005-03-30 This may not be needed if the message tags work well.
/**
List of integers containing the command numbers (zero index) where warnings
occurred - filled in processCommands().
*/
private List __command_warning_Vector = new Vector ();

/**
The number of commands that are at the beginning of the __commands, which have
been automatically added (e.g., a setWorkingDir() command to initialize the working directory).
*/
private int __num_prepended_commands = 0;

/**
The internal list of StateCU_BlaneyCriddle being processed.
*/
private List<StateCU_BlaneyCriddle> __CUBlaneyCriddle_Vector = new Vector();

/**
The internal list of StateCU_ClimateStation being processed.
*/
private List<StateCU_ClimateStation> __CUClimateStation_Vector = new Vector();

/**
The internal list of StateCU_CropCharacteristics being processed.
*/
private List<StateCU_CropCharacteristics> __CUCropCharacteristics_Vector = new Vector();

/**
The internal list of StateCU_CropPatternTS being processed.
The supplemental data are filled by the setCropPatternTS() commands.
The supplemental data are now stored as raw parcel data and are summed
at a ditch level when requested for use.
*/
private List<StateCU_CropPatternTS> __CUCropPatternTS_Vector = new Vector();
//private List __HydroBase_Supplemental_StructureIrrigSummaryTS_Vector = new Vector();

/**
The internal list of StateCU_Location being processed.
*/
private List<StateCU_Location> __CULocation_Vector = new Vector();

/**
The internal list of StateCU_Location being processed.
*/
private List<StateCU_PenmanMonteith> __CUPenmanMonteith_Vector = new Vector();

/**
The internal list of StateCU_IrrigationPracticeTS being processed.
The supplemental data are supplied with readIrrigationPracticeTSFromList and
are used by readIrrigationPracticeTSFromHyroBase().
*/
private List<StateCU_IrrigationPracticeTS> __CUIrrigationPracticeTS_Vector = new Vector();
private List __HydroBase_Supplemental_ParcelUseTS_Vector = new Vector();

/**
The internal list of TS contain Agricultural Statistics (AgStats)...
*/
private List __CUAgStatsTS_Vector = new Vector();

/**
The internal list of HydroBase_ParcelUseTS records that can be examined.  These
are either read from one or more DBF files or from HydroBase.   These data are
mainly used when processing well to parcel relationships.
*/
// TODO SAM 2007-02-18 Evaluate if needed
//private List __CUParcelUseTS_Vector = new Vector();

/**
The internal list of HydroBase_StructureToParcel records that can be examined.
These are either read from one or more DBF files or from HydroBase.   These data
are mainly used when processing well to parcel relationships.
*/
// TODO SAM 2007-02-18 Evaluate if needed
//private Vector __CUStructureToParcel_Vector = new Vector();

/**
The StateMod data set that is used to track processing when commands are run.
Separate lists of data are maintained in this command processor and are set in the data
set components.
*/
private StateMod_DataSet __StateMod_DataSet = null;

/**
The StateCU data set that is used to track processing when commands are run.
*/
private StateCU_DataSet __StateCU_DataSet = null;

/**
The internal list of StateMod_StreamGage being processed.
*/
private List<StateMod_StreamGage> __SMStreamGageStationList = new Vector();

/**
The internal list of StateMod_DelayTable (monthly) being processed.
*/
private List<StateMod_DelayTable> __SMDelayTableMonthlyList = new Vector();

/**
The internal list of StateMod_DelayTable (daily) being processed.
*/
private List<StateMod_DelayTable> __SMDelayTableDailyList = new Vector();

/**
The internal list of StateMod_Diversion being processed.
*/
private List<StateMod_Diversion> __SMDiversionStationList = new Vector();

/**
The internal list of StateMod_DiversionRight being processed.
*/
private List<StateMod_DiversionRight> __SMDiversionRightList = new Vector();

/**
The internal list of StateMod diversion time series (monthly) being processed.
*/
private List<MonthTS> __SMDiversionTSMonthlyList = new Vector();

/**
The internal list of StateMod diversion time series (monthly) being processed.
This is a saved version to be used to check for observations when using the
LimitDiversionHistoricalTSMonthlyToRights() command.
*/
private List<MonthTS> __SMDiversionTSMonthly2List = new Vector();

/**
The internal list of monthly pattern time series used for data filling.
*/
private List<StringMonthTS> __SMPatternTSMonthlyList = new Vector();

/**
The internal list of StateMod daily historical TS being processed.
*/
private List<DayTS> __SMDiversionTSDailyList = new Vector();

/**
The internal list of StateMod demand time series (monthly) being processed.
*/
private List<MonthTS> __SMDemandTSMonthlyList = new Vector();

/**
The internal list of StateMod demand time series (daily) being processed.
*/
private List<DayTS> __SMDemandTSDailyList = new Vector();

/**
The internal list of StateMod consumptive water requirement time series (monthly) being processed.
*/
private List<MonthTS> __SMConsumptiveWaterRequirementTSMonthlyList = new Vector();

/**
The internal list of StateMod_Reservoir being processed.
*/
private List<StateMod_Reservoir> __SMReservoirStationList = new Vector();

/**
The internal list of StateMod_ReservoirRight being processed.
*/
private List<StateMod_ReservoirRight> __SMReservoirRightList = new Vector();

/**
The internal list of reservoir StateMod_ReturnFlow being processed.
*/
private List<StateMod_ReturnFlow> __SMReservoirReturnList = new Vector();

/**
The internal list of StateMod_InstreamFlow being processed.
*/
private List<StateMod_InstreamFlow> __SMInstreamFlowStationList = new Vector();

/**
The internal list of StateMod_InstreamFlowRight being processed.
*/
private List<StateMod_InstreamFlowRight> __SMInstreamFlowRightList = new Vector();

/**
The internal list of StateMod instream flow demand TS (average monthly) being processed.
*/
private List<MonthTS> __SMInstreamFlowDemandTSAverageMonthlyList = new Vector();

/**
The internal list of StateMod_Well being processed.
*/
private List<StateMod_Well> __SMWellList = new Vector();

/**
The internal list of StateMod_WellRight being processed.
*/
private List<StateMod_WellRight> __SMWellRightList = new Vector();

/**
The internal list of StateMod well historical pumping time series (monthly) being processed.
*/
private List<MonthTS> __SMWellHistoricalPumpingTSMonthlyList = new Vector();

/**
The internal list of StateMod well demand time series (monthly) being processed.
*/
private List<MonthTS> __SMWellDemandTSMonthlyList = new Vector();

/**
The internal list of StateMod_Plan being processed.
*/
private List<StateMod_Plan> __SMPlanList = new Vector();

/**
The internal list of StateMod_Plan_WellAugmentation being processed.
*/
private List<StateMod_Plan_WellAugmentation> __SMPlanWellAugmentationList = new Vector();

/**
The internal list of StateMod_ReturnFlow being processed.
*/
private List<StateMod_ReturnFlow> __SMPlanReturnList = new Vector();

/**
The internal list of StateMod_StreamEstimate being processed.
*/
private List<StateMod_StreamEstimate> __SMStreamEstimateStationList = new Vector();

/**
The internal list of StateMod_StreamEstimate_Coefficients being processed.
*/
private List<StateMod_StreamEstimate_Coefficients> __SMStreamEstimateCoefficients_Vector = new Vector();

/**
The internal list of StateMod_PrfGageData used when processing stream estimate coefficients.
*/
private List<StateMod_PrfGageData> __SMPrfGageData_Vector = new Vector();

/**
The internal list of StateMod_RiverNetworkNode being processed.
*/
private List<StateMod_RiverNetworkNode> __SMRiverNetworkNode_Vector = new Vector();

/**
The internal list of StateMod_OperationalRight being processed.
*/
private List<StateMod_OperationalRight> __SMOperationalRightList = new Vector();

/**
The internal StateMod_Network that defines the StateMod model network (not
to be confused with the StateMod network file).
*/
private StateMod_NodeNetwork __SM_network = null;

// Dynamic memory to keep track of data objects that are matched during
// processing, resulting in updates...

private List<String> __CUBlaneyCriddle_match_Vector = new Vector();
private List<String> __CUClimateStation_match_Vector = new Vector();
private List<String> __CUCropCharacteristics_match_Vector = new Vector();
private List<String> __CUCropPatternTS_match_Vector = new Vector();
private List<String> __CUDelayTableAssignment_match_Vector = new Vector();
private List<String> __CULocation_match_Vector = new Vector();
private List<String> __CUIrrigationPracticeTS_match_Vector = new Vector();
private List<String> __CUDelayTableDaily_match_Vector = new Vector();
private List<String> __CUDelayTableMonthly_match_Vector = new Vector();
private List<String> __CUPenmanMonteith_match_Vector = new Vector();

private List<String> __SMStreamGage_match_Vector = new Vector();
private List<String> __SMDelayTableMonthly_match_Vector = new Vector();
private List<String> __SMDelayTableDaily_match_Vector = new Vector();
private List<String> __SMDiversion_match_Vector = new Vector();
private List<String> __SMDiversionRight_match_Vector = new Vector();
private List<String> __SMDiversionTSMonthly_match_Vector = new Vector();
// TODO SAM 2007-02-18 Enable if needed
//private List __SMDiversionTSDaily_match_Vector = new Vector();
private List<String> __SMConsumptiveWaterRequirementTSMonthly_match_Vector = new Vector();
private List<String> __SMDemandTSMonthly_match_Vector = new Vector();
private List<String> __SMReservoir_match_Vector = new Vector();
private List<String> __SMReservoirRight_match_Vector = new Vector();
private List<String> __SMReservoirReturn_match_Vector = new Vector();
private List<String> __SMInstreamFlow_match_Vector = new Vector();
private List<String> __SMInstreamFlowRight_match_Vector = new Vector();
private List<String> __SMInstreamFlowDemandTSAverageMonthly_match_Vector = new Vector();
private List<String> __SMWell_match_Vector = new Vector();
private List<String> __SMWellRight_match_Vector = new Vector();
private List<String> __SMWellHistoricalPumpingTSMonthly_match_Vector = new Vector();
private List<String> __SMWellDemandTSMonthly_match_Vector = new Vector();
private List<String> __SMPlan_match_Vector = new Vector();
private List<String> __SMPlanReturn_match_Vector = new Vector();
private List<String> __SMPlanWellAugmentation_match_Vector = new Vector();
private List<String> __SMStreamEstimate_match_Vector = new Vector();
private List<String> __SMStreamEstimateCoefficients_match_Vector = new Vector();
private List<String> __SMPrfGageData_match_Vector = new Vector();
private List<String> __SMRiverNetworkNode_match_Vector = new Vector();
private List<String> __SMOperationalRight_match_Vector = new Vector();

/**
The HydroBase DMI instance that is used for database queries.
*/
private HydroBaseDMI __hdmi = null;

/**
Default width for WDIDs (2 for WD, 5 for ID).  For now automatically override internally based on
what is read from existing data.
*/
private int __defaultWdidLength = 7;

private ProcessListener [] __listeners = null;	// Listeners to receive process output.

/**
Internal data that is used to set/get data so new DateTime objects don't need to
be created each time.  The object has full precision - other objects with
different precision may need to be created.
*/
private DateTime __temp_DateTime = new DateTime();

/**
Gets set to true if a run has occurred and was successful.  It is to false at the beginning of a
run and is only set to true if run is successful.  Needed for running in batch mode for validation of runs.
*/
private boolean runSuccessful = false;

private int __data_check_count = 0;

/**
Construct a command processor with no commands.
*/
public StateDMI_Processor ()
{	super();
}

/**
StateDMI_Processor Constructor.
@param gui the gui in which this processor is running
@param hbdmi HydroBaseDMI instance for database I/O.
@param commands the list of commands to execute.
@param num_prepended_commands The count of the commands at the start of
"commmands" that have been automatically added and which should not be indicated
in the count in messages.  It is assumed that these commands run "flawlessly" so
as to not confuse the user.
@param app_PropLIst properties from the application (e.g., containing the
initial "WorkingDir".
*/
/* FIXME SAM 2008-12-05 Does not appear to be needed - confirm that can be removed
public StateDMI_Processor (	StateDMI_JFrame gui, HydroBaseDMI hbdmi,
		List commands, int num_prepended_commands,
				PropList app_PropList )
{	setCommands ( commands );
	__gui = gui;
	__hdmi = hbdmi;
	__app_PropList = app_PropList;
	__num_prepended_commands = num_prepended_commands;
	initialize();
}
*/

/**
Add a command at the end of the list and notify command list listeners of the
add.
@param command Command to add.
*/
public void addCommand ( Command command )
{
	addCommand ( command, true );
}

/**
Add a command at the end of the list.
@param command Command to add.
@param notifyCommandListListeners Indicate whether registered CommandListListeners should be notified.
*/
public void addCommand ( Command command, boolean notifyCommandListListeners )
{	String routine = getClass().getName() + ".addCommand";
	getCommands().add( command );
	// Also add this processor as a listener for events
	if ( command instanceof CommandProcessorEventProvider ) {
	    CommandProcessorEventProvider ep = (CommandProcessorEventProvider)command;
	    ep.addCommandProcessorEventListener(this);
	}
	if ( notifyCommandListListeners ) {
		notifyCommandListListenersOfAdd ( getCommands().size() - 1, getCommands().size() - 1 );
	}
	Message.printStatus(2, routine, "Added command object \"" +	command + "\"." );
}

/**
Add a command at the end of the list using the string text.  This should currently only be
used for commands that do not have command classes, which perform
additional validation on the commands.  A GenericCommand instance will
be instantiated to maintain the string and allow command status to be set.
@param command_string Command string for command.
@param index Index (0+) at which to insert the command.
*/
public void addCommand ( String command_string )
{	String routine = "StateDMI_Processor.addCommand";
	Command command = new GenericCommand ();
	command.setCommandString ( command_string );
	addCommand ( command );
	Message.printStatus(2, routine, "Creating generic command from string \"" + command_string + "\"." );
}

/**
Add a CommandListListener, to be notified when commands are added, removed,
or change (are edited or execution status is updated).
If the listener has already been added, the listener will remain in
the list in the original order.
*/
public void addCommandListListener ( CommandListListener listener )
{
	// Use arrays to make a little simpler than Vectors to use later...
	if ( listener == null ) {
		return;
	}
	// See if the listener has already been added...
	// Resize the listener array...
	int size = 0;
	if ( __CommandListListener_array != null ) {
		size = __CommandListListener_array.length;
	}
	for ( int i = 0; i < size; i++ ) {
		if ( __CommandListListener_array[i] == listener ) {
			return;
		}
	}
	if ( __CommandListListener_array == null ) {
		__CommandListListener_array = new CommandListListener[1];
		__CommandListListener_array[0] = listener;
	}
	else {
		// Need to resize and transfer the list...
		size = __CommandListListener_array.length;
		CommandListListener [] newlisteners =
			new CommandListListener[size + 1];
		for ( int i = 0; i < size; i++ ) {
			newlisteners[i] = __CommandListListener_array[i];
		}
		__CommandListListener_array = newlisteners;
		__CommandListListener_array[size] = listener;
	}
}

/**
Add a CommandProcessorEventListener, to be notified when commands generate CommandProcessorEvents.
This is currently utilized by the check file capability, which queues events and generates a report file.
If the listener has already been added, the listener will remain in
the list in the original order.
TODO SAM 2008-08-21 Make this private for now but may need to rethink if other than the check file use
the events.
*/
private void addCommandProcessorEventListener ( CommandProcessorEventListener listener )
{
    // Use arrays to make a little simpler than Vectors to use later...
    if ( listener == null ) {
        return;
    }
    // See if the listener has already been added...
    // Resize the listener array...
    int size = 0;
    if ( __CommandProcessorEventListener_array != null ) {
        size = __CommandProcessorEventListener_array.length;
    }
    for ( int i = 0; i < size; i++ ) {
        if ( __CommandProcessorEventListener_array[i] == listener ) {
            return;
        }
    }
    if ( __CommandProcessorEventListener_array == null ) {
        __CommandProcessorEventListener_array = new CommandProcessorEventListener[1];
        __CommandProcessorEventListener_array[0] = listener;
    }
    else {
        // Need to resize and transfer the list...
        size = __CommandProcessorEventListener_array.length;
        CommandProcessorEventListener [] newlisteners = new CommandProcessorEventListener[size + 1];
        for ( int i = 0; i < size; i++ ) {
            newlisteners[i] = __CommandProcessorEventListener_array[i];
        }
        __CommandProcessorEventListener_array = newlisteners;
        __CommandProcessorEventListener_array[size] = listener;
    }
}

/**
Add a CommandProcessorListener, to be notified when commands are started,
progress made, and completed.  This is useful to allow calling software to report progress.
If the listener has already been added, the listener will remain in
the list in the original order.
*/
public void addCommandProcessorListener ( CommandProcessorListener listener )
{
	// Use arrays to make a little simpler than Vectors to use later...
	if ( listener == null ) {
		return;
	}
	// See if the listener has already been added...
	// Resize the listener array...
	int size = 0;
	if ( __CommandProcessorListener_array != null ) {
		size = __CommandProcessorListener_array.length;
	}
	for ( int i = 0; i < size; i++ ) {
		if ( __CommandProcessorListener_array[i] == listener ) {
			return;
		}
	}
	if ( __CommandProcessorListener_array == null ) {
		__CommandProcessorListener_array = new CommandProcessorListener[1];
		__CommandProcessorListener_array[0] = listener;
	}
	else {	// Need to resize and transfer the list...
		size = __CommandProcessorListener_array.length;
		CommandProcessorListener [] newlisteners = new CommandProcessorListener[size + 1];
		for ( int i = 0; i < size; i++ ) {
			newlisteners[i] = __CommandProcessorListener_array[i];
		}
		__CommandProcessorListener_array = newlisteners;
		__CommandProcessorListener_array[size] = listener;
	}
}

/**
Add a ProcessListener to receive Process output.  Multiple listeners can be
registered.  If an attempt is made to register the same listener more than
once, the later attempt is ignored.
@param listener ProcessListener to add.
*/
public void addProcessListener ( ProcessListener listener )
{	// Use arrays to make a little simpler than Vectors to use later...
	if ( listener == null ) {
		return;
	}
	// See if the listener has already been added...
	// Resize the listener array...
	int size = 0;
	if ( __listeners != null ) {
		size = __listeners.length;
	}
	for ( int i = 0; i < size; i++ ) {
		if ( __listeners[i] == listener ) {
			return;
		}
	}
	if ( __listeners == null ) {
		__listeners = new ProcessListener[1];
		__listeners[0] = listener;
	}
	else {
		// Need to resize and transfer the list...
		size = __listeners.length;
		ProcessListener [] newlisteners = new ProcessListener[size + 1];
		for ( int i = 0; i < size; i++ ) {
			newlisteners[i] = __listeners[i];
		}
		__listeners = newlisteners;
		__listeners[size] = listener;
	}
}

/**
Calculate a structure's capacity, for assignment from HydroBase.
The order of assignment is as follows:
<ol>
<li>	The estimated capacity is used if not missing.</li>
<li>	The decreed capacity is used if not missing.</li>
<li>	A value of 999 is used.</li>
</ol>
@param id The identifier for the main station being analyzed (used for messages).
@param capacity0 Original capacity (or StateMod_Util.MISSING_DOUBLE if missing).
@param hbdiv HydroBase_Structure used with W&D node types where wells supplement
diversion supply (null if processing an explicit well or well-only aggregate/system).
@param ditch_cov Fraction of a diversion's irrigation of a parcel (only used
when processing well structures or permits).
@param hbwell HydroBase_Structure for a well structure, used when a supplemental
well is a structure (null when processing diversions).
@param hbwell_parcel HydroBase_Wells for a well, used when a supplemental
well is a well permit (null when processing diversions).
@param collection_type the collection type for "id" or blank if not a
collection (used for messages).
@param part_id Identifier for the part being processed.
@param comp_type Data set component:  StateMod_DataSet.COMP_DIVERSION_STATIONS
or StateMod_DataSet.COMP_WELL_STATIONS.
@param add If true, then add to the capacity.  If false, reset.
*/
private double calculateStructureCapacity (	double capacity0,
						String id,
						HydroBase_Structure hbdiv,
						double ditch_cov,
						HydroBase_Structure hbwell,
						HydroBase_Wells hbwell_parcel,
						String collection_type,
						String part_id,
						int comp_type,
						boolean add )
{	String routine = "StateDMI_Processor.calculateStructureCapacity";
	String station_type = " diversion ";
	if ( comp_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
		station_type = " well ";
	}
	String part_string = "";
	if ( collection_type.length() > 0 ) {
		part_string = collection_type + " part: " + part_id;
	}
	double capacity = 0.0;
	if ( hbdiv == null ) {
		// Well is not tied to a ditch.
		ditch_cov = 1.0;
	}
	if ( (hbdiv != null) && (hbwell == null) && (hbwell_parcel == null) ) {
		// Processing a diversion station...
		if ( hbdiv.getEst_capacity() > 0.0 ) {
			capacity = hbdiv.getEst_capacity();
			Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" estimated capacity -> " +
			StringUtil.formatString(capacity,"%.2f"));
		}
		else if ( hbdiv.getDcr_capacity() > 0.0 ) {
			capacity = hbdiv.getDcr_capacity();
			Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" decreed capacity -> " +
			StringUtil.formatString(capacity,"%.2f") );
		}
		else {	// Default 999 value as per watright
			// code
			// TODO SAM 2004-06-08 may need a
			// better way to handle but often gets
			// reset to the maximum diversion value.
			capacity = 999.0;
			Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" default capacity -> 999" );
		}
	}
	else if ( hbwell != null ) {
		// Processing a well structure...
		if ( hbwell.getEst_capacity() > 0.0 ) {
			capacity = hbwell.getEst_capacity()*ditch_cov*
				hbwell_parcel.getPercent_yield();
			if ( hbdiv == null ) {
			// No ditch coverage...
			Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" well WDID=" +
			HydroBase_WaterDistrict.formWDID(hbwell.getWD(),
			hbwell.getID()) +
			" estimated capacity*percent_yield: " +
			StringUtil.formatString(hbwell.getEst_capacity(),"%.4f")
			+ "*" +
			StringUtil.formatString(
			hbwell_parcel.getPercent_yield(),"%.4f") + " -> " +
			StringUtil.formatString(capacity,"%.2f"));
			}
			else {
			Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" well WDID=" +
			HydroBase_WaterDistrict.formWDID(hbwell.getWD(),
			hbwell.getID()) +
			" estimated capacity*ditch_cov*percent_yield: " +
			StringUtil.formatString(hbwell.getEst_capacity(),"%.4f")
			+ "*" +
			StringUtil.formatString(ditch_cov,"%.4f")
			+ "*" +
			StringUtil.formatString(
			hbwell_parcel.getPercent_yield(),"%.4f") + " -> " +
			" -> " + StringUtil.formatString(capacity,"%.2f"));
			}
		}
		else if ( hbwell.getDcr_capacity() > 0.0 ) {
			capacity = hbwell.getDcr_capacity()*ditch_cov*
				hbwell_parcel.getPercent_yield();
			if ( hbdiv == null ) {
			// No ditch coverage...
			Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" decreed capacity*percent_yield: " +
			StringUtil.formatString(hbwell.getDcr_capacity(),"%.4f")
			+ "*" +
			StringUtil.formatString(ditch_cov,"%.4f")
			+ "*" +
			StringUtil.formatString(
			hbwell_parcel.getPercent_yield(),"%.4f") + " -> " +
			" -> " + StringUtil.formatString(capacity,"%.2f") );
			}
			else {
			// Include ditch coverage...
			Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" decreed capacity*ditch_cov*percent_yield: " +
			StringUtil.formatString(hbwell.getDcr_capacity(),"%.4f")
			+ "*" +
			StringUtil.formatString(ditch_cov,"%.4f")
			+ "*" +
			StringUtil.formatString(
			hbwell_parcel.getPercent_yield(),"%.4f") + " -> " +
			StringUtil.formatString(capacity,"%.2f") );
			}
		}
		else {	Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" yield -> NO DATA." );
			return capacity0;
		}
	}
	else if ( hbwell_parcel != null ) {
		// Processing a "well permit"...
		if ( hbwell_parcel.getYield() > 0.0 ) {
			capacity = hbwell_parcel.getYield()
				*.002228	// GPM to CFS
				*ditch_cov
				*hbwell_parcel.getPercent_yield();
			if ( hbdiv == null ) {
			// No diversion...
			Message.printStatus ( 2, routine,
			"Using " + id +
			" receipt=" + hbwell_parcel.getReceipt()+
			" class=" + hbwell_parcel.getClass() +
			" yield*.002228CFS/GPM*percent_yield: " +
			StringUtil.formatString(
				hbwell_parcel.getPercent_yield(),"%.4f") +
				"*.002228*" +
				StringUtil.formatString(
				hbwell_parcel.getPercent_yield(),"%.4f") +
				" -> " +
				StringUtil.formatString(capacity,"%.2f") );
			}
			else {
			// With diversion...
			Message.printStatus ( 2, routine,
			"Using " + id +
			" receipt=" + hbwell_parcel.getReceipt()+
			" class=" + hbwell_parcel.getClass() +
			" yield*.002228CFS/GPM*ditch_cov*percent_yield: " +
			StringUtil.formatString(
				hbwell_parcel.getPercent_yield(),"%.4f") +
				"*.002228*" + ditch_cov + "*" +
				StringUtil.formatString(
				hbwell_parcel.getPercent_yield(),"%.4f") +
				" -> " +
				StringUtil.formatString(capacity,"%.2f") );
			}
		}
		else {	Message.printStatus ( 2, routine,
			"Using " + id + station_type + part_string +
			" yield -> NO DATA." );
			return capacity0;
		}
	}
	if ( !add || StateMod_Util.isMissing(capacity0) ) {
		return capacity;
	}
	else {	return capacity0 + capacity;
	}
}

// TODO SAM 2005-08-15
// Should this method search the commands for write*() commands, in order to
// better decide which warnings are important?  Currently some checks are only
// done when debug is turned on.
/**
Check the integrity of the data set.  The following checks are always done:
<ol>
<li>	A location cannot be in zero or one of the following, for the entire
	data set:  aggregate, system, MultiStruct.
	</li>
</ol>
The following checks are only done but do not result in "important" warnings:
<ol>
<li>	If diversion stations and historical monthly time series are in memory,
	check whether each station has a time series, and check whether there
	are time series that do not match an ID.
	</li>
</ol>
@return the number of warnings that are displayed.
*/
// TODO SAM 2007-02-18 Evaluate how used in check code
/*
private int checkDataSet ()
{	int warning_count = 0, warning_count_fyi = 0;
	int wl = 2;
	int wl_fyi = 3;
	String command_tag = "EndChecks";	// See StateDMI_JFrame.
						// goToMessageTag
	String routine = "StateDMI_Processor.checkDataSet";

	Message.printStatus ( 2, routine, __LOG_THICK_LINE );
	Message.printStatus ( 2, routine,
	"Checking data set for consistency, correctness..." );

	// Loop through the StateCU locations, first checking for duplicate
	// IDs and then checking to make sure that identifiers used in
	// aggregates are only used in one place.

	StateCU_Location culoc_i, culoc_j;
	StateMod_Diversion dds_i;
	String id_i, id_j;
	TS ts_i;
	int pos;
	int size = __CULocation_Vector.size();
	for ( int i = 0; i < size; i++ ) {
		culoc_i = (StateCU_Location)__CULocation_Vector.get(i);
		id_i = culoc_i.getID();
		for ( int j = 0; j < size; j++ ) {
			if ( i == j ) {
				// Don't compare to itself...
				continue;
				// TODO SAM 2005-08-15
				// Could put warnings here about a location
				// calling itself a collection - alternate
				// identifiers are advised.
			}
			culoc_j = (StateCU_Location)
				__CULocation_Vector.get(j);
			id_j = culoc_j.getID();
			// Simple check to make sure the ID is not repeated...
			if ( id_i.equalsIgnoreCase(id_j) ) {
				Message.printWarning ( wl,
				formatMessageTag(command_tag,++warning_count),
				routine,
				"CU Location \"" + id_i + "\" (position "+
				(i + 1) + " is repeated in position " +(j + 1));
			}
		}
	}

	// If debug is on, do some additional checks...

	// TODO SAM 2005-08-16
	// For now always do this but at a lower warning level.
	//if ( Message.isDebugOn ) {

	if (	(__SMDiversion_Vector.size() > 0) &&
		(__SMDiversionTSMonthly_Vector.size() > 0) ) {
		// Diversion stations and historical monthly time series are
		// in the data set.

		// Make sure that there is a diversion time series for every
		// location...

		size = __SMDiversion_Vector.size();
		for ( int i = 0; i < size; i++ ) {
			dds_i = (StateMod_Diversion)
				__SMDiversion_Vector.get(i);
			id_i = dds_i.getID();
			pos = TSUtil.indexOf(
				__SMDiversionTSMonthly_Vector,
				id_i, "Location", 0 );
			if ( pos < 0 ) {
				Message.printWarning ( wl_fyi,
				formatMessageTag(command_tag,
				++warning_count_fyi), routine,
				"Diversion station \"" + id_i + "\" does not " +
				"have a historical monthly time series." );
			}
		}

		// Check to see whether there are extra time series that do
		// not correspond to stations...

		size = __SMDiversionTSMonthly_Vector.size();
		for ( int i = 0; i < size; i++ ) {
			ts_i = (TS)__SMDiversionTSMonthly_Vector.get(i);
			id_i = ts_i.getLocation();
			pos = StateMod_Util.indexOf(
				__SMDiversion_Vector, id_i );
			if ( pos < 0 ) {
				Message.printWarning ( wl_fyi,
				formatMessageTag(command_tag,
				++warning_count_fyi), routine,
				"Diversion historical time series (monthly) " +
				"for \"" + id_i + "\" does not " +
				"have a diversion station." );
			}
		}
	}

	// Make sure well stations have rights...

	/ * TODO SAM 2006-04-10
	May not need this if checks occurring at write are OK.
	if (	__StateMod_DataSet.getComponentForComponentType(
		StateMod_DataSet.COMP_WELL_RIGHTS).isOutput() ) {
		// Make sure that there is at least one well right for each well
		// station...

		size = __SMWell_Vector.size();
		message_list = new Vector();
		for ( int i = 0; i < size; i++ ) {
			wes_i = (StateMod_Well)
				__SMWell_Vector.get(i);
			id_i = wes_i.getID();
			rights = StateMod_Util.getRights (
				id_i, __SMWellRight_Vector );
			if ( (rights == null) || (rights.size() == 0) ) {
				// Format suitable for output in a list that
				// can be copied to a spreadsheet or table.
				message_list.add (
				StringUtil.formatString(id_i,"%-12.12s") +
				", \"" + wes_i.getName() + "\"" );
			}
		}
		size = message_list.size();
		if ( size > 0 ) {
			// Need to notify the user...
			message_list.insertget ( "The following well " +
			"stations have no water rights.", 0 );
			message_list.insertget ( "", 1 );
			message_list.insertget ( "     ID     , NAME", 2);
			for ( int i = 0; i < size; i++ ) {
				Message.printWarning ( wl_fyi,
				formatMessageTag(command_tag,
				++warning_count_fyi), routine,
				(String)message_list.get(i) );
			}
		}
		__StateMod_DataSet.getComponentForComponentType(
			StateMod_DataSet.COMP_WELL_RIGHTS).setDataCheckResults(
			message_list);
	}
	* /

	//} // End if debug is on.

	Message.printStatus ( 2, routine,
	"Done checking data set for consistency." );
	Message.printStatus ( 2, routine, __LOG_THICK_LINE );

	return warning_count;
}
*/

/**
Clear the results of processing.  This resets the lists of data objects to empty.
*/
public void clearResults()
{
	__CUBlaneyCriddle_Vector.clear();
	__CUClimateStation_Vector.clear();
	__CUCropCharacteristics_Vector.clear();
	__CUCropPatternTS_Vector.clear();
	__CULocation_Vector.clear();
	__CUIrrigationPracticeTS_Vector.clear();
	__CUPenmanMonteith_Vector.clear();

	// TODO SAM 2007-06-15 Remove after using parcels only is verified
	//__HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.clear();
	__HydroBase_Supplemental_ParcelUseTS_Vector.clear();

	__SMDelayTableMonthlyList.clear();
	__SMDelayTableDailyList.clear();
	__SMDiversionStationList.clear();
	__SMDiversionRightList.clear();
	__SMDiversionTSMonthlyList.clear();
	__SMDiversionTSMonthly2List.clear();
	__SMPatternTSMonthlyList.clear();
	__SMConsumptiveWaterRequirementTSMonthlyList.clear();
	__SMDemandTSMonthlyList.clear();
	__SMDemandTSDailyList.clear();
	__SM_network = null;	// Will be retrieved from GUI if necessary.
	__SMOperationalRightList.clear();
	__SMPlanList.clear();
	__SMPlanWellAugmentationList.clear();
	__SMPlanReturnList.clear();
	__SMReservoirReturnList.clear();
	__SMReservoirStationList.clear();
	__SMReservoirRightList.clear();
	__SMRiverNetworkNode_Vector.clear();
	__SMInstreamFlowStationList.clear();
	__SMInstreamFlowRightList.clear();
	__SMInstreamFlowDemandTSAverageMonthlyList.clear();
	__SMStreamGageStationList.clear();
	__SMStreamEstimateStationList.clear();
	__SMStreamEstimateCoefficients_Vector.clear();
	__SMPrfGageData_Vector.clear();
	__SMWellList.clear();
	__SMWellRightList.clear();
	__SMWellHistoricalPumpingTSMonthlyList.clear();
	__SMWellDemandTSMonthlyList.clear();
}

/**
Convert the supplemental ParcelUseTS data (raw data specified by set commands)
to StructureIrrigSummary records (used for ditched processing).
This method is called from a couple of commands.  Data records are set with SetCropPatternTS() and
SetCropPatternTSFromList() commands and are later used to create crop patterns when
ReadCropPatternTSFromHydroBase() is called.
*/
protected List convertSupplementalParcelUseTSToStructureIrrigSummaryTS (
	List supplementalParcelUseTSList )
{	String routine = "StateDMI_Processor.convertSupplementalParcelUseTSToStructureIrrigSummaryTS";
	List HydroBase_Supplemental_StructureIrrigSummaryTS_Vector = new Vector();
	int size_parcels = supplementalParcelUseTSList.size();
	StateDMI_HydroBase_ParcelUseTS parcel = null;
	StateDMI_HydroBase_StructureView sits = null;
	boolean found = false; // Whether a matching summary is found
	// Loop through the raw parcel data...
	for ( int iparcel = 0; iparcel < size_parcels; iparcel++ ){
		parcel = (StateDMI_HydroBase_ParcelUseTS)supplementalParcelUseTSList.get(iparcel);
		// Find a matching location in irrig summary data and add to it...
		found = false;
		/* TODO SAM 2007-06-17 Evaluate whether needed - old code does
		not have true irrig summary ts but simple records.
		for ( int isummary = 0; isummary < size_summary; isummary++ ) {
			summary = (StateDMI_HydroBase_StructureView)HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.get(isummary);
			if ( parcel.getLocationID().equalsIgnoreCase(summary.getLocationID()) {
				found = true;
				break;
			}
		}
		*/
		if ( !found ) {
			// Create a new summary to add to
			sits = new StateDMI_HydroBase_StructureView();
		}
		sits.setLocationID ( parcel.getLocationID() );
		if ( HydroBase_WaterDistrict.isWDID(parcel.getLocationID())) {
			try {
				int [] wdid_parts = new int[2];
				HydroBase_WaterDistrict.parseWDID(parcel.getLocationID());
				sits.setWD ( wdid_parts[0] );
				sits.setID ( wdid_parts[1] );
			}
			catch ( Exception e ) {
				//Absorb - just use the LocationID
			}
		}
		// Set the IrrigSummaryTS information using only the total area...
		sits.setCal_year ( parcel.getCal_year() );
		sits.setLand_use ( parcel.getLand_use() );
		sits.setAcres_total ( parcel.getArea() );
		HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.add(sits);
	}
	Message.printStatus ( 2, routine,
		"Created " + HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.size() +
		" structure view/irrig_summary_ts records from " + supplementalParcelUseTSList.size() +
		" supplemental parcel records:" );
	for ( int i = 0; i < HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.size(); i++ ) {
		sits = (StateDMI_HydroBase_StructureView)HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.get(i);
		Message.printStatus ( 2, routine, "Location=" + sits.getLocationID() +
			" Year=" + sits.getCal_year() +
			" Crop=" + sits.getLand_use() +
			" Area=" + StringUtil.formatString(sits.getAcres_total(),"%.3f") );
	}
	return HydroBase_Supplemental_StructureIrrigSummaryTS_Vector;
}

/**
Create a list of HydroBase_StructureIrrigSummaryTS (new is HydroBase_StructureView) from lists of
HydroBase_ParcelUseTS and HydroBase_StructureToParcel data.  This is used when
processing a DBF file into CropPatternTS.
@return a list of HydroBase_StructureIrrigSummaryTS.
@param command_tag Command tag used for messaging.
@parma warning_count Warning count used for messaging.
@param parcelusets_Vector A list of HydroBase_ParcelUseTS.
@param struct2parcel_Vector A list of HydroBase_StructureToParcel.
*/
private List createIrrigSummaryTS (	String command_tag, int warning_count,
		List parcelusets_Vector,
		List struct2parcel_Vector )
{	String routine = "StateDMI_Processor.createIrrigSummaryTS";
	List irrigsummaryts_Vector = new Vector();

	// Loop through the HydroBase_StructureToParcel (new is
	// HydroBase_StructurView) records...

	int size = 0;
	if ( struct2parcel_Vector != null ) {
		size = struct2parcel_Vector.size();
	}
	HydroBase_StructureView sits = null;
	HydroBase_StructureToParcel stp;
	HydroBase_ParcelUseTS pts = null;
	int pts_size = 0;	// Size of ParcelUseTS
	if ( parcelusets_Vector != null ) {
		pts_size = parcelusets_Vector.size();
	}
	int sits_size = 0;
	boolean found;	// Used for searches.
	String stp_id="";// Structure ID in ParcelUseTS
	int ipts;	// To iterate on ParcelUseTS
	int isits;	// To iterate on StructureIrrigSummaryTS (new is
			// HydroBase_StructureView)
	int stp_parcelid = 0;	// Parcel ID referenced by StructureToParcel,
				// assumed to be in one division.
	String land_use = "";	// for parcel
	String irrig_type = "";	// for parcel
	int cal_year = 0;	// for parcel
	double area = 0.0;	// for parcel
	double stp_percent_irrig;	// for structure_to_parcel
	// Loop through structure_to_parcel records, retrieving parcel data as
	// necessary to create a irrig_summary_ts object...
	for ( int i = 0; i < size; i++ ) {
		stp = (HydroBase_StructureToParcel)
			struct2parcel_Vector.get(i);
		stp_id = stp.getStructure_id();
		stp_parcelid = stp.getParcel_id();
		stp_percent_irrig = stp.getPercent_irrig();
		// Get the parcel information for the relationship
		found = false;
		for ( ipts = 0; ipts < pts_size; ipts++ ) {
			pts = (HydroBase_ParcelUseTS)
				parcelusets_Vector.get(ipts);
			if ( pts.getParcel_id() == stp_parcelid ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Did not find the parcel for the structure...
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Could not find parcel " + stp_parcelid +
			" to compute irrig_summary_ts for structure " +
			stp_id );
			continue;
		}
		// Data from the parcel...
		area = pts.getArea();
		cal_year = pts.getCal_year();
		irrig_type = pts.getIrrig_type();
		land_use = pts.getLand_use();
		// For debugging...
		//Message.printStatus ( 2, routine,
		//"structure to parcel " + stp_id + "," + stp_parcelid +
		//" matches parcel_use_ts " + pts.getParcel_id() + "," +
		//area );
		// See if the structure exists in the list already.  It must
		// match the structure_id, crop type, and year.
		found = false;
		sits_size = irrigsummaryts_Vector.size();
		for ( isits = 0; isits < sits_size; isits++ ) {
			sits = (HydroBase_StructureView)
				irrigsummaryts_Vector.get(isits);
			if (	sits.getStructure_id().equalsIgnoreCase(stp_id)
				&& sits.getLand_use().equalsIgnoreCase(land_use)
				&& (sits.getCal_year() == cal_year) ){
				found = true;
				break;
			}
		}
		// StructureIrrigSummaryTS did not exist, so add one...
		if ( !found ) {
			sits = new HydroBase_StructureView();
			sits.setStructure_id(stp_id);
			sits.setStructure_id(stp_id);
			sits.setCal_year ( cal_year );
			sits.setLand_use ( land_use );
			if ( irrig_type.equalsIgnoreCase("DRIP") ) {
				sits.setAcres_by_drip (area*stp_percent_irrig );
			}
			else if ( irrig_type.equalsIgnoreCase("FLOOD") ) {
				sits.setAcres_by_flood (area*stp_percent_irrig);
			}
			else if ( irrig_type.equalsIgnoreCase("FURROW") ) {
				sits.setAcres_by_furrow(area*stp_percent_irrig);
			}
			else if ( irrig_type.equalsIgnoreCase("SPRINKLER") ) {
				sits.setAcres_by_sprinkler (
					area*stp_percent_irrig);
			}
			irrigsummaryts_Vector.add ( sits );
		}
		else {	// Structure record exists, so add to its acreage...
			double old_value;
			if ( irrig_type.equalsIgnoreCase("DRIP") ) {
				old_value = sits.getAcres_by_drip();
				if ( old_value < 0.0 ) {
					sits.setAcres_by_drip ( area*
					stp_percent_irrig );
				}
				else {	sits.setAcres_by_drip ( old_value +
					area*stp_percent_irrig );
				}
			}
			else if ( irrig_type.equalsIgnoreCase("FLOOD") ) {
				old_value = sits.getAcres_by_flood();
				if ( old_value < 0.0 ) {
					sits.setAcres_by_flood ( area*
					stp_percent_irrig );
				}
				else {	sits.setAcres_by_flood ( old_value +
					area*
					stp_percent_irrig );
				}
			}
			else if ( irrig_type.equalsIgnoreCase("FURROW") ) {
				old_value = sits.getAcres_by_furrow();
				if ( old_value < 0.0 ) {
					sits.setAcres_by_furrow ( area*
					stp_percent_irrig );
				}
				else {	sits.setAcres_by_furrow ( old_value +
					area*stp_percent_irrig );
				}
			}
			else if ( irrig_type.equalsIgnoreCase("SPRINKLER") ) {
				old_value = sits.getAcres_by_sprinkler();
				if ( old_value < 0.0 ) {
					sits.setAcres_by_sprinkler (
					area* stp_percent_irrig );
				}
				else {	sits.setAcres_by_sprinkler ( old_value+
					area*stp_percent_irrig );
				}
			}
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( 10, routine,
			"Setting irrig_summary_ts for id=" + stp_id +
			" year=" + cal_year +
			"crop type=" + land_use +
			" flood=" + sits.getAcres_by_flood() +
			" sprinkler=" + sits.getAcres_by_sprinkler() +
			" total=" + sits.getAcres_total() );
		}
	}
	// Now loop through and sum up the acres by various irrigation methods
	// into a total acreage...
	size = irrigsummaryts_Vector.size();
	double total;
	double value;
	for ( int i = 0; i < size; i++ ) {
		sits = (HydroBase_StructureView)
			irrigsummaryts_Vector.get(i);
		total = 0.0;
		found = false;	// Indicate if some data found.
		value = sits.getAcres_by_drip();
		if ( !DMIUtil.isMissing(value) ) {
			total += value;
			found = true;
		}
		value = sits.getAcres_by_flood();
		if ( !DMIUtil.isMissing(value) ) {
			total += value;
			found = true;
		}
		value = sits.getAcres_by_furrow();
		if ( !DMIUtil.isMissing(value) ) {
			total += value;
			found = true;
		}
		value = sits.getAcres_by_sprinkler();
		if ( !DMIUtil.isMissing(value) ) {
			total += value;
			found = true;
		}
		if ( found ) {
			sits.setAcres_total ( total );
		}
	}
	// Notify of errors...
	if ( warning_count > 0 ) {
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count), routine,
		"There were errors calculating ditch crop patterns." );
	}
	return irrigsummaryts_Vector;
}

/**
Read agricultural statistics time series from a DateValue file.
@param command the command being executed:
<pre>
readAgStatsTSFromDateValue(InputFile="x")
</pre>
@exception if an error occurs.
*/
private void do_readAgStatsTSFromDateValue (String command_tag, String command)
throws Exception
{	String routine = "StateDMI_Processor.do_readAgStatsTSFromDateValue",
		message;
	int warning_count = 0;

	List tokens = StringUtil.breakStringList ( command, "()", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || tokens.size() < 2 ) {
		// Must have at least the command name and file...
		message = "Bad command \"" + command +
			"\".  Expecting 2+ tokens.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}

	// Get the input needed to process the file...
	PropList props = PropList.parse ( (String)tokens.get(1), routine, "," );
	String InputFile = props.getValue ( "InputFile" );

	String full_filename = IOUtil.getPathUsingWorkingDir ( InputFile );
	if ( !IOUtil.fileExists(full_filename) ) {
		message = "Input file \"" + full_filename +
			"\" does not exist.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new Exception ( message );
	}

	try {	__CUAgStatsTS_Vector =
			DateValueTS.readTimeSeriesList ( full_filename,
			__OutputStart_DateTime, __OutputEnd_DateTime, null, true );
		if (	(__CUAgStatsTS_Vector == null) ||
			(__CUAgStatsTS_Vector.size() == 0) ) {
			message = "No time series were read from \"" +
			full_filename + "\".";
			Message.printWarning ( 2,
			formatMessageTag(command_tag, ++warning_count),
			routine,message);
		}

	}
	catch ( Exception e ) {
		message = "Error reading file \"" + full_filename +
			"\".";
		Message.printWarning ( 2,
		formatMessageTag(command_tag, ++warning_count),routine,message);
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings processing the command.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new Exception ( message );
	}
}

/**
This method serves two purposes:
<ol>
<li>	The primary purpose is to process DBF file associated with a parcels
	shapefile, to create data in the CDS file.</li>
<li>	The secondary purpose is to process a text list file containing parcel
	data irrigated by sprinklers.  These "txt" files were produced in the
	Rio Grande and have a very similar structure to the DBF file.</li>
</ol>
Any matched structures can have the
data reset from that found in the DBF or text file.  The data processing occurs
as follows when processing the DBF:
<ol>
<li>	The data in the DBF are first divided into HydroBase_ParcelUseTS and
	HydroBase_StructureToParcel records.</li>
<li>	These are then processed into HydroBase_StructureIrrigSummaryTS
	(new is HydroBase_StructureView) records.</li>
<li>	The logic is then very similar to readCropPatternTSFromHydroBase(),
	where searches are done based on location ID and data are added to the
	time series.</li>
</ol>
@param command_tag Command tag used for messaging.
@param command Command to process:
<pre>
readCropPatternTSFromDBF(DBFFile="x",ID="X",...)
readSprinklerParcelsFromList(ListFile="x",ID="X",...)
</pre>
@param do_patterns If true, then a crop patterns DBF file is being processed.
If false, a sprinkler parcels list file is being processed.
*/
/* FIXME SAM 2009-02-11 Enable when ready
private void do_readCropParcels ( String command_tag, String command,
		boolean do_patterns)
throws Exception
{	// Even though this does not match the actual method name, use a fake
	// method name to make it easier to read the log file...
	String routine = "StateDMI_Processor.do_readCropPatternTSFromDBF",
		message;
	int warning_count = 0;
	if ( !do_patterns ) {
		// Reassign so that messages make sense...
		routine = "StateDMI_Processor.do_readSprinklerParcelsFromList";
	}

	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source (e.g., CDS, HydroBase).
	// This is used to print a warning.

	if ( do_patterns ) {
		resetDataMatches ( __CUCropPatternTS_match_Vector );
	}

	List tokens = StringUtil.breakStringList ( command, "()", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || tokens.size() < 2 ) {
		// Must have at least the command name and file...
		message = "Bad command \"" + command +
			"\".  Expecting 2 tokens.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}

	// Get the input needed to process the file...
	// Unless otherwise noted, properties are used for both types of
	// input...
	PropList props = PropList.parse ( (String)tokens.get(1), routine, "," );
	// Allow a sublist of structures to be processed...
	String ID = props.getValue ( "ID" );	// Patterns only
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String DBFFile = props.getValue ( "DBFFile" );
						// Patterns only
	String ListFile = props.getValue ( "ListFile" );
						// Sprinklers only
	String Div = props.getValue ( "Div" );
	String Year = props.getValue ( "Year" );// Patterns only
	String YearCol = props.getValue ( "YearCol" );
	String ParcelIDYear = props.getValue ( "ParcelIDYear" );
						// Sprinkler only
	String ParcelIDCol = props.getValue ( "ParcelIDCol" );
	String AreaCol = props.getValue ( "AreaCol" );
	String AreaUnits = props.getValue ( "AreaUnits" );
	String CropTypeCol = props.getValue ( "CropTypeCol" );
						// Patterns only
	String IrrigTypeCol = props.getValue ( "IrrigTypeCol" );
						// Patterns only
	String DitchIDCols = props.getValue ( "DitchIDCols" );
	String DitchCovCols = props.getValue ( "DitchCovCols" );
	String GWOnlyCol = props.getValue ( "GWOnlyCol" );
						// Patterns only
	String ExcludedCropTypes = props.getValue ( "ExcludedCropTypes" );
						// Patterns only
	String ProcessData = props.getValue ( "ProcessData" );
						// Patterns only.  If true, then
						// process the data into StateCU
						// output.  If false, read the
						// data but only for the purpose
						// of getting
						// __CUParcelUseTS_Vector and
						// __CUStructureToParcel_Vector.

	if ( __hdmi == null ) {
		throw new Exception ( "No HydroBase connection" );
	}

	List ExcludedCropTypes_Vector = null;
	if ( do_patterns ) {
		if ( DBFFile == null ) {
			message = "The DBF file must be specified.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
		DBFFile = IOUtil.getPathUsingWorkingDir(DBFFile);
		if ( ExcludedCropTypes != null ) {
			ExcludedCropTypes_Vector =
			StringUtil.breakStringList ( ExcludedCropTypes,	", ", StringUtil.DELIM_SKIP_BLANKS );
		}
	}
	else {	// Sprinkler...
		if ( ListFile == null ) {
			message = "The list file must be specified.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
		ListFile = IOUtil.getPathUsingWorkingDir(ListFile);
	}

	if ( (Div == null) || !StringUtil.isInteger(Div) ) {
		message = "The value for Div must be an integer.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}
	int div = StringUtil.atoi ( Div );

	if ( AreaUnits == null ) {
		AreaUnits = "ACRE";
	}

	if ( (__OutputStart_DateTime == null) || (__OutputEnd_DateTime == null) ) {
		// The output period is necessary to initialize the time
		// series...
		if ( do_patterns ) {
			message = "The output period must be defined before " +
			"reading crop pattern time series from a DBF file.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
		else {	message = "The output period must be defined before " +
			"reading sprinkler parcel data.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
	}

	// Read the tabular data file...

	DataTable table = null;
	if ( do_patterns ) {
		try {	table = new DbaseDataTable(DBFFile, true, false);
		}
		catch ( Exception e ) {
			message = "Error reading DBF \"" + DBFFile + "\"";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
	}
	else {	// Sprinklers...
		try {	PropList table_props = new PropList ("");
			table_props.set ( "Delimiter=," );
			table_props.set ( "CommentLineIndicator=#" );
			table = DataTable.parseFile (ListFile, table_props);
		}
		catch ( Exception e ) {
			message = "Error reading list file \"" + ListFile +"\"";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
	}

	// Figure out the columns that are needed...

	int parcelidcol = 0;
	int parcelidcoltype = 0;
	if ( ParcelIDCol == null ) {
		message = "The ParcelIDCol value must be specified.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}
	try {	parcelidcol = table.getFieldIndex(ParcelIDCol);
		parcelidcoltype = table.getTableFieldType(parcelidcol);
	}
	catch ( Exception e ) {
		message = "The ParcelIDCol value \"" + ParcelIDCol +
			"\" does not match a column in the DBF file.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}

	int year = -1;
	int yearcol = 0;
	int yearcoltype = 0;
	if ( do_patterns ) {
		if ( Year != null ) {
			if ( !StringUtil.isInteger(Year) ) {
				message =
				"The value for Year must be an integer.";
				Message.printWarning ( 2,
				formatMessageTag(command_tag,++warning_count),
				routine, message );
				throw new Exception ( message );
			}
			else {	year = StringUtil.atoi ( Year );
			}
		}
		if ( year < 0 ) {
			// Try to get the column for the year...
			if (	(YearCol == null) ||
				!StringUtil.isInteger(YearCol) ) {
				message =
				"The value for YearCol must be an integer.";
				Message.printWarning ( 2,
				formatMessageTag(command_tag,++warning_count),
				routine, message );
				throw new Exception ( message );
			}
			try {	yearcol = table.getFieldIndex(YearCol);
				yearcoltype = table.getTableFieldType(yearcol);
			}
			catch ( Exception e ) {
				message = "The YearCol value \"" + YearCol +
				"\" does not match a column in the DBF file.";
				Message.printWarning ( 2,
				formatMessageTag(command_tag,++warning_count),
				routine, message );
				throw new Exception ( message );
			}
		}
	}
	else {	// Sprinkler parcels needs Year...
		if ( (Year == null) || !StringUtil.isInteger(Year) ) {
			message =
			"The value for Year must be an integer.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
		else {	year = StringUtil.atoi ( Year );
		}
	}

	int parcelidyear = 0;
	if ( !do_patterns ) {
		// Sprinkler parcels needs ParcelIDYear...
		if (	(ParcelIDYear == null) ||
			!StringUtil.isInteger(ParcelIDYear) ) {
			message =
			"The value for ParcelIDYear must be an integer.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
		else {	parcelidyear = StringUtil.atoi ( ParcelIDYear );
		}
	}

	int areacol = 0;
	int areacoltype = 0;
	if ( AreaCol == null ) {
		message = "The AreaCol value must be specified.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}
	try {	areacol = table.getFieldIndex(AreaCol);
		areacoltype = table.getTableFieldType(areacol);
	}
	catch ( Exception e ) {
		message = "The AreaCol value \"" + AreaCol +
			"\" does not match a column in the DBF file.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}

	int croptypecol = 0;
	if ( do_patterns ) {
		if ( CropTypeCol == null ) {
			message = "The CropTypeCol value must be specified.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
		try {	croptypecol = table.getFieldIndex(CropTypeCol);
		}
		catch ( Exception e ) {
			message = "The CropTypeCol value \"" + CropTypeCol +
			"\" does not match a column in the DBF file.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
	}

	int irrigtypecol = 0;
	if ( do_patterns ) {
		if ( IrrigTypeCol == null ) {
			message = "The IrrigTypeCol value must be specified.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
		try {	irrigtypecol = table.getFieldIndex(IrrigTypeCol);
		}
		catch ( Exception e ) {
			message = "The IrrigTypeCol value \"" + IrrigTypeCol +
				"\" does not match a column in the DBF file.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
	}

	// Figure out the columns for the ditch supply...

	if ( DitchIDCols == null ) {
		message = "The DitchIDCols value must be specified.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}
	tokens = StringUtil.breakStringList ( DitchIDCols,
		", ", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || (tokens.size() == 0) ) {
		message = "The DitchIDCols value must be specified.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}
	int size = tokens.size();
	int [] ditchidcols = new int[size];
	for ( int i = 0; i < size; i++ ) {
		try {	ditchidcols[i] = table.getFieldIndex(
				(String)tokens.get(i));
		}
		catch ( Exception e ) {
			message = "The DitchIDCols value \"" +
				(String)tokens.get(i) +
				"\" does not match a column in the DBF file.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
	}

	if ( DitchCovCols == null ) {
		message = "The DitchCovCols value must be specified.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}
	tokens = StringUtil.breakStringList ( DitchCovCols,
		", ", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || (tokens.size() == 0) ) {
		message = "The DitchCovCols value must be specified.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
		throw new Exception ( message );
	}
	size = tokens.size();
	int [] ditchcovcols = new int[size];
	int [] ditchcovcoltypes = new int[size];
	for ( int i = 0; i < size; i++ ) {
		try {	ditchcovcols[i] = table.getFieldIndex(
				(String)tokens.get(i));
			ditchcovcoltypes[i] =
				table.getTableFieldType(ditchcovcols[i]);
		}
		catch ( Exception e ) {
			message = "The DitchCovCols value \"" +
				(String)tokens.get(i) +
				"\" does not match a column in the DBF file.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
		}
	}

	if ( ditchidcols.length != ditchcovcols.length ) {
		message = "The DitchIDCols \"" + DitchIDCols
			+ "\" and DitchCovCols \"" + DitchCovCols +
			"\" lists have different lengths";
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine, message );
			throw new Exception ( message );
	}

	int gwonlycol = -1;
	if ( do_patterns ) {
		if ( GWOnlyCol != null ) {
			try {	gwonlycol = table.getFieldIndex(GWOnlyCol);
			}
			catch ( Exception e ) {
				message = "The GWOnlyCol value \"" + GWOnlyCol +
				"\" does not match a column in the DBF file.";
				Message.printWarning ( 2,
				formatMessageTag(command_tag,++warning_count),
				routine, message );
				throw new Exception ( message );
			}
		}
	}

	// Process the DBF records into HydroBase_* objects...

	int numrows = table.getNumberOfRecords();
	HydroBase_ParcelUseTS pts;
	List parcelusets_Vector = new Vector();
	HydroBase_StructureToParcel struct2parcel;
	List struct2parcel_Vector = new Vector();
	Integer Integer_value;
	Double Double_value;
	int parcel_id = 0;
	String land_use = "";		// Crop type.
	String irrig_type = "";		// Irrigation type.
	int cal_year = 0;
	double area = 0.0;		// Area for a parcel.
	double ditchcov = 0.0;		// Percentage of a parcel that a ditch
					// irrigates.
	int iditch;			// Loop index for ditches for parcels.
	String gwonly;			// Flag indicating whether parel only
					// has groundwater supply
	String ditchid;			// A ditch supplying a parcel
	Object o;
	int exclude_size = 0;
	if ( ExcludedCropTypes != null ) {
		exclude_size = ExcludedCropTypes_Vector.size();
	}
	int iec;			// Loop counter for excluded crops.
	boolean need_to_exclude;	// If true, the parcel needs to be
					// excluded (e.g., crop type is not
					// irrigated).
	String excluded_crop;		// Crop type to exclude.
	for ( int row = 0; row < numrows; row++ ) {
		try {	// First transfer data into HydroBase_ParcelUseTS
			// objects, one object per record.  If reading from a
			// DBF the numeric data should normally be in numeric
			// fields types.  However, sometimes the types are not
			// as expected.  Also, all fields read from a text
			// file are strings and need to be converted...

			// Check land use first because it may be excluded...
			// Land use (crop type)...
			if ( do_patterns ) {
				land_use = ((String)table.
				getFieldValue(row,croptypecol)).
				trim().toUpperCase();
				if ( ExcludedCropTypes != null ) {
					// Check to see whether this parcel
					// needs to be excluded based on the
					// crop type...
					need_to_exclude = false;
					for(iec = 0; iec < exclude_size; iec++){
						excluded_crop = (String)
						ExcludedCropTypes_Vector.
						get(iec);
						if (	excluded_crop.
							equalsIgnoreCase(
							land_use) ) {
							need_to_exclude = true;
							break;
						}
					}
					if ( need_to_exclude ) {
						// Do not add the parcel...
						continue;
					}
				}
			}
			else {	// For sprinkler parcel analysis, only want
				// acreage for all crops...
				land_use = "AllCrops";
			}
			// Parcel ID...
			o = table.getFieldValue(row,parcelidcol);
			if ( parcelidcoltype == TableField.DATA_TYPE_INT ) {
				Integer_value = (Integer)o;
				parcel_id = Integer_value.intValue();
			}
			else if ( parcelidcoltype==TableField.DATA_TYPE_DOUBLE){
				Double_value = (Double)o;
				// Make sure that roundoff is correct...
				parcel_id=(int)(Double_value.doubleValue() +.1);
			}
			else if (parcelidcoltype==TableField.DATA_TYPE_STRING) {
				parcel_id = StringUtil.atoi((String)o);
			}
			// Always make sure that the parcel ID is set...
			if ( parcel_id == 0 ) {
				Message.printWarning ( 2,
				formatMessageTag(command_tag,++warning_count),
				routine, "Data record " + (row + 1) +
				" does not have a parcel ID or ID = 0. " +
				"Sprinkler irrigated but not in later year..." +
				" skipping." );
				continue;
			}
			// Calendar year for data...
			if ( year >= 0 ) {
				cal_year = year;
			}
			else {	o = table.getFieldValue(row,yearcol);
				if ( yearcoltype == TableField.DATA_TYPE_INT ) {
					Integer_value = (Integer)o;
					cal_year = Integer_value.intValue();
				}
				else if ( yearcoltype==
					TableField.DATA_TYPE_DOUBLE){
					Double_value = (Double)o;
					// Make sure that roundoff is correct...
					cal_year = (int)
					(Double_value.doubleValue() + .1);
				}
				else if (yearcoltype==
					TableField.DATA_TYPE_STRING) {
					cal_year = StringUtil.atoi((String)o);
				}
			}
			// Crop area....
			o = table.getFieldValue(row,areacol);
			if ( areacoltype== TableField.DATA_TYPE_DOUBLE){
				area = ((Double)o).doubleValue();
			}
			else if ( areacoltype== TableField.DATA_TYPE_INT ) {
				area = (double)(((Integer)o).intValue());
			}
			else if (areacoltype== TableField.DATA_TYPE_STRING) {
				area = StringUtil.atod((String)o);
			}
			if ( do_patterns ) {
				irrig_type = ((String)table.
					getFieldValue(row,irrigtypecol)).
					trim().toUpperCase();
			}
			else {	// For sprinkler, know that the method is
				// Sprinkler...
				irrig_type = "SPRINKLER";
			}
			if ( do_patterns && (gwonlycol >= 0) ) {
				// Use this below to increase performance.
				gwonly = ((String)table.
				getFieldValue(row,gwonlycol)).trim();
			}
			// If here, create a new instance, set the data values,
			// and add to the vector...
			pts = new HydroBase_ParcelUseTS();
			pts.setParcel_id ( parcel_id );
			pts.setLand_use(land_use);
			pts.setDiv ( div );
			pts.setCal_year ( cal_year );
			pts.setArea ( area );
			pts.setIrrig_type ( irrig_type );
			parcelusets_Vector.add ( pts );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 10, routine,
				"Adding parcel_use_ts record for parcel_id=" +
				parcel_id + " year=" + cal_year + " area=" +
				area );
			}

			// Next transfer data into HydroBase_StructureToParcel
			// objects, zero or more objects per record, based on
			// whether supply information is available...

			for (iditch = 0; iditch < ditchidcols.length; iditch++){
				// Check the groundwater only flag...
				if ( do_patterns && (gwonlycol >= 0) ) {
					gwonly = ((String)table.
					getFieldValue(row,gwonlycol)).trim();
					if ( gwonly.equalsIgnoreCase("yes") ) {
						// No need to evaluate ditch
						// supply...
						continue;
					}
				}
				// If here, we need to check the ditch
				// identifiers.
				// Get the ditch ID...
				ditchid = ((String)table.
					getFieldValue(row,ditchidcols[iditch])).
					trim().toUpperCase();
				if (	(ditchid.length() == 0) ||
					(!do_patterns && ditchid.equals("0"))) {
					// Zero is special case found in the
					// Rio Grande NNSPRINK.txt files.
					continue;
				}
				// Else add a StructureToParcel record...
				if ( ditchcovcoltypes[iditch] ==
					TableField.DATA_TYPE_DOUBLE){
					ditchcov = ((Double)table.
					getFieldValue(row,ditchcovcols[
					iditch])).doubleValue();
				}
				else if ( ditchcovcoltypes[iditch] ==
					TableField.DATA_TYPE_STRING){
					ditchcov = StringUtil.atod(
						((String)table.
					getFieldValue(row,ditchcovcols[
					iditch])).trim() );
				}
				struct2parcel =
					new HydroBase_StructureToParcel();
				struct2parcel.setDiv ( div );
				struct2parcel.setParcel_id ( parcel_id );
				struct2parcel.setCal_year ( cal_year );
				struct2parcel.setStructure_id ( ditchid );
				struct2parcel.setPercent_irrig ( ditchcov );

				// Mark the parcel as dirty since it is
				// associated with a structure.  If the
				// resulting irrig_summary_ts is not used, a
				// warning will be printed that in effect
				// covers the parcel.

				pts.setDirty ( true );

				if ( Message.isDebugOn ) {
					Message.printDebug ( 10, routine,
					"Adding IrrigStructureTS for parcel_id "
					+ parcel_id + " year=" + cal_year +
					" ditchid=" + ditchid +
					" ditchcov=" + ditchcov );
				}

				struct2parcel_Vector.add(struct2parcel);
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2,
				formatMessageTag(command_tag,++warning_count),
				routine,
				"Error processing record " + (row + 1) +
				" ... skipping" );
			Message.printWarning ( 3, routine, e );
			continue;
		}
	}

	Message.printStatus ( 2, routine, "Created " +
		parcelusets_Vector.size() + " parcel_use_ts records." );
	Message.printStatus ( 2, routine, "Created " +
		struct2parcel_Vector.size() + " structure_to_parcel records." );

	// Now create the internal HydroBase_StructureIrrigSummaryTS (new is
	// HydroBase_StructureView) list...

	List irrigsummaryts_Vector = createIrrigSummaryTS ( command_tag,
		warning_count, parcelusets_Vector, struct2parcel_Vector);

	Message.printStatus ( 2, routine, "Created " +
		irrigsummaryts_Vector.size() + " irrig_summary_ts records." );

	// TODO SAM 2004-03-12 - need to merge these for multiple years,
	// using the div and
	// year.  It may be possible that an old year is read from HydroBase and
	// a new year from a draft DBF file...

	// Save the list of parcel use time series in case they are needed to
	// process wells, etc...

	// TODO SAM 2007-02-18 Maybe help with performance?
	//__CUParcelUseTS_Vector = parcelusets_Vector;

	// Similar save for structure_to_parcel...

	// TODO SAM 2007-02-18 Maybe help with performance?
	//__CUStructureToParcel_Vector = struct2parcel_Vector;

	if ( (ProcessData != null) && ProcessData.equalsIgnoreCase("false") ) {
		// No need to continue because only the input data are needed.
		// Do check for errors...
		if ( warning_count > 0 ) {
			message = "There were " + warning_count +
				" warnings processing the command.";
			Message.printWarning ( 2,
			formatMessageTag(command_tag, ++warning_count),
			routine,message);
			throw new Exception ( message );
		}
		// otherwise just return...
		return;
	}

	// Because the aggregates and processing well parcels cause some data
	// management complexities, process each structure individually.  This
	// will be slower than if all structures are queried at once, but the
	// logic is cleaner...

	size = 0;
	if ( __CULocation_Vector != null ) {
		size = __CULocation_Vector.size();
	}
	StateCU_Location culoc = null;			// CU Location that has
							// crop pattern TS
	HydroBase_StructureView h_cds;			// HydroBase data
	HydroBase_ParcelUseTS h_parcel;			// HydroBase parcel data
	List culoc_wdids = new Vector ( 100 );
	String culoc_id = null;
	List crop_patterns = null;	// Crop pattern records from HydroBase
	int ih, hsize;			// Counter and size for HydroBase
					// records.
	String units = AreaUnits;	// Units for area
	int replace_flag = 0;		// 0 to replace data in time series or
					// 1 to add
	List collection_ids;		// IDs that are part of an
					// aggregate/system
	int [] collection_ids_array;	// collection_ids as an int array.
	int [] collection_years;	// Years corresponding to the collection
					// definitions.
	String part_id;			// One ID from an aggregate/sytem.
	int collection_size;		// Size of a collection.
	
	// Convert supplemental ParcelUseTS to StructureIrrigSummaryTS
	
	// FIXME SAM 2009-02-11 Enable the following
	
	List HydroBase_Supplemental_StructureIrrigSummaryTS_Vector =
		convertSupplementalParcelUseTSToStructureIrrigSummaryTS(getHydroBaseSupplementalParcelUseTSList());
	for ( int i = 0; i < size; i++ ) {
		culoc = (StateCU_Location)__CULocation_Vector.get(i);
		culoc_id = culoc.getID();
		if ( do_patterns && !culoc_id.matches(idpattern_Java) ) {
			// Identifier does not match...
			continue;
		}
		// Else for sprinklers process all...

		// Check to see if the location is a simple diversion node,
		// an aggregate/system diversion, or a well aggregate/diversion,
		// and process accordingly.

		try {
		if ( !culoc.isCollection() ) {
			// If single diversion...
			Message.printStatus ( 1, routine,
			"Processing single diversion \"" + culoc_id + "\"" );
			culoc_wdids.clear();
			/ * TODO SAM 2007-06-17 Remove if code below works as expected
			if (	do_patterns &&
				!HydroBase_WaterDistrict.isWDID(culoc_id) ) {
				Message.printStatus ( 2, routine,
				"Skipping single diversion \"" + culoc_id +
				"\" - it is not a WDID - use " +
				"setCropPatternTS(...,ProcessWhen=Now,...).");
				continue;
			}
			* /
			// Add the single structure...
			culoc_wdids.add ( culoc_id );	
			if ( HydroBase_WaterDistrict.isWDID(culoc_id) ) {
				// Find data in the list...
				crop_patterns = findStructureIrrigSummaryTSListForWDIDListLand_usePeriod
				( irrigsummaryts_Vector, culoc_wdids,
				__OutputStart_DateTime, __OutputEnd_DateTime );
			}
			// Add supplemental records (works whether a WDID or not)...
			crop_patterns = readSupplementalStructureIrrigSummaryTSListForWDIDList (
				crop_patterns, culoc_wdids, __OutputStart_DateTime,
				__OutputEnd_DateTime,
				HydroBase_Supplemental_StructureIrrigSummaryTS_Vector );
			// The results are processed below...
			replace_flag = 0;
		}

		else if ( culoc.isCollection() && culoc.getCollectionPartType().
			equalsIgnoreCase("Ditch")){
			Message.printStatus ( 2, routine,
			"Processing diversion aggregate/system \"" + culoc_id +
			"\"" );
			// Aggregate/system diversion...
			// Put together a list of WDIDs from the current CU
			// location.  Currently ditch aggregate/systems are not
			// allowed to vary over time so request the aggregate
			// information for year 0...

			collection_ids = culoc.getCollectionPartIDs(0);
			collection_size = 0;
			if ( collection_ids != null ) {
				collection_size = collection_ids.size();
			}
			culoc_wdids.clear();
			for ( int j = 0; j < collection_size; j++ ) {
				part_id = (String)
					collection_ids.get(j);
				if ( !HydroBase_WaterDistrict.isWDID(part_id)){
					Message.printWarning ( 2,
					formatMessageTag(command_tag,
					++warning_count),
					routine,
					"CU location \"" + culoc_id +
					"\" part \"" + part_id +
					"\" is not a WDID.  Check command." );
					continue;
				}
				culoc_wdids.add ( part_id );
			}

			// Read from the internal list of data records...

			crop_patterns =
			findStructureIrrigSummaryTSListForWDIDListLand_usePeriod
				( irrigsummaryts_Vector, culoc_wdids,
				__OutputStart_DateTime, __OutputEnd_DateTime );

			// TODO SAM 2004-05-18 - should this be isolated only
			// to crop patterns?

			// Add supplemental records...

			crop_patterns =
			readSupplementalStructureIrrigSummaryTSListForWDIDList (
				crop_patterns, culoc_wdids, __OutputStart_DateTime,
				__OutputEnd_DateTime,
				HydroBase_Supplemental_StructureIrrigSummaryTS_Vector);

			if ( do_patterns ) {

				// First find the matching CropPatternTS and
				// clear out the existing contents.
				/ *
				TODO - SAM 2004-05-18 - why is this done?
				Comment out for now

				pos = StateCU_Util.indexOf (
					__CUCropPatternTS_Vector, culoc_id);
				if ( pos >= 0 ) {
					((StateCU_CropPatternTS)
					__CUCropPatternTS_Vector.
					get(pos)).removeAllTS();
				}
				* /
			}

			// Process the records below into the collection ID...

			replace_flag = 1;	// 1 means add
		}
		else if ( culoc.isCollection() && culoc.getCollectionPartType().
			equalsIgnoreCase(
			StateMod_Well.COLLECTION_PART_TYPE_PARCEL)){
			// Well aggregate/system...
			Message.printStatus ( 2, routine,
			"Processing well aggregate/system \"" + culoc_id +
			"\"" );

			if ( do_patterns ) {
				// Clear the existing time series contents.
				/ *
				TODO - SAM 2004-05-18 - why was this done?
				Comment out for now and run tests.

				pos = StateCU_Util.indexOf (
					__CUCropPatternTS_Vector, culoc_id);
				if ( pos >= 0 ) {
					((StateCU_CropPatternTS)
					__CUCropPatternTS_Vector.
					get(pos)).removeAllTS();
				}
				* /

				// Put together a list of parcel IDs from the
				// current CU location.  The aggregate/systems
				// are allowed to vary over time so read the
				// parcels for the specific years for which
				// parcel groups are defined.  Later the missing
				// data can be filled.

				collection_years = culoc.getCollectionYears();
				if ( collection_years == null ) {
					return;
				}
			}
			else {	// Processing sprinkler parcels and a specific
				// year must be specified for the parcel IDs.
				// To allow using the same code below, use an
				// array with one item...
				collection_years = new int[1];
				collection_years[0] = parcelidyear;
			}
			for ( int iy = 0; iy < collection_years.length; iy++ ) {
				collection_ids = culoc.getCollectionPartIDs(
					collection_years[iy]);
				collection_size = 0;
				collection_ids_array = null;
				if ( collection_ids != null ) {
					collection_size = collection_ids.size();
					collection_ids_array =
						new int[collection_size];
					for (	int ic = 0;
						ic < collection_size; ic++ ) {
						part_id = (String)
							collection_ids.
							get(ic);
						if (	!StringUtil.isInteger(
							part_id)) {
							Message.printWarning (2,
							formatMessageTag(
							command_tag,
							++warning_count),
							routine,
							"CU location \"" +
							culoc_id +
							"\" part ID \"" +
							part_id +
							"\" is not an integer. "
							+ "Check command" );
							// Should not return
							// anything
							// from HydroBase...
							collection_ids_array[ic]
								= -1;
						}
						else {	collection_ids_array[ic]
							= StringUtil.atoi (
							(String)collection_ids.
							get(ic) );
						}
					}
				}

				if ( collection_ids_array == null ) {
					Message.printWarning ( 2,
					formatMessageTag( command_tag,
					warning_count), routine,
					"CU location \"" + culoc_id +
					"\" has no aggregate/system parts.  " +
					"Check command" );
					continue;
				}

				// Read from internal list...

				crop_patterns =
					findParcelUseTSListForParcelList(
					parcelusets_Vector,
					culoc.getCollectionDiv(),// Division
					collection_ids_array,	// parcel ids
					__OutputStart_DateTime, __OutputEnd_DateTime);

				// Process the records below into the
				// collection ID...

				replace_flag = 1;	// 1 means add
				hsize = 0;
				if ( crop_patterns != null ) {
					hsize = crop_patterns.size();
				}
				Message.printStatus ( 2, routine,
					"Processing " + hsize +
					" parcel_use_ts records" );

				for ( ih = 0; ih < hsize; ih++) {
					h_parcel = (HydroBase_ParcelUseTS)
						crop_patterns.get( ih);
					if ( do_patterns ) {
						// Filter out lands that are not
						// irrigated...
						irrig_type =
						h_parcel.getIrrig_type();
						// TODO SAM 2004-03-01 -
						// don't want to hard-code
						// strings but need to handle
						// revisions in HydroBaseDMI -
						// ref_irrig_type should
						// indicate whether irrigated
						if (	irrig_type.
							equalsIgnoreCase("NA")){
							// Does not irrigate...
							continue;
						}
						// Replace or add in the
						// __CUCropPatternTS_Vector...
						// Pass individual fields
						// because we may or may not
						// need to add a new
						// StateCU_CropPatternTS or a
						// time series in the object...
						findAndAddCUCropPatternTSValue (
							culoc_id, "" +
							h_parcel.getParcel_id(),
							h_parcel.getCal_year(),
							h_parcel.getParcel_id(),
							h_parcel.getLand_use(),
							h_parcel.getArea(),
							__OutputStart_DateTime,
							__OutputEnd_DateTime,
							units, replace_flag );
					}
					/ * FIXME SAM 2007-10-18 Remove when code tests out.
					else {	// Process sprinkler parcels...
						// TODO - fix hard-coded
						// year type
						findAndAddCUIrrigationPracticeSprinklerTSValue(
							culoc_id,
							h_parcel.getCal_year(),
							h_parcel.getLand_use(),
							h_parcel.getArea(),
							__OutputStart_DateTime,
							__OutputEnd_DateTime,
							"CYR", replace_flag );
					}
					* /
					// Mark as dirty so we know what
					// was used...
					h_parcel.setDirty ( true );
				}
			}

			continue;	// Code below is for diversions.
		}

		// If here, a Vector of HydroBase objects is defined for the CU
		// Location and can be added to the StateCU_CropPatternTS data.
		// If an aggregate, a list of records is put together above.
		// This code is only used for diversions because wells are
		// processed above.

		hsize = 0;
		if ( crop_patterns != null ) {
			hsize = crop_patterns.size();
		}
		Message.printStatus ( 2, routine,
			"Processing " + hsize + " irrig_summary_ts records" );

		for ( ih = 0; ih < hsize; ih++) {
			h_cds = (HydroBase_StructureView)crop_patterns.get(ih);

			if ( do_patterns ) {
				// Replace or add in the
				// __CUCropPatternTS_Vector...
				// Pass individual fields because we may or may
				// not need to add a new StateCU_CropPatternTS
				// or a time series in the object...
				findAndAddCUCropPatternTSValue (
					culoc_id,
					h_cds.getStructure_id(),
					h_cds.getCal_year(),
					-1,	// No individual parcel IDs are available
					h_cds.getLand_use(),
					h_cds.getAcres_total(),
					__OutputStart_DateTime,
					__OutputEnd_DateTime,
					units, replace_flag );
				// TODO SAM 2007-05-29 Evaluate whether individual parcels
				// need processed to allow data checks.
			}
			else {	// Process sprinkler data...
				/ * FIXME SAM 2007-10-18 Remove when code tests out.
				findAndAddCUIrrigationPracticeSprinklerTSValue(
					culoc_id,
					h_cds.getCal_year(),
					h_cds.getLand_use(),
					h_cds.getAcres_total(),
					__OutputStart_DateTime,
					__OutputEnd_DateTime,
					units, replace_flag );
					* /
			}
			// Mark as dirty so we know what was used...
			h_cds.setDirty ( true );
		}
		}
		catch ( Exception e ) {
			if ( do_patterns ) {
				Message.printWarning ( 2,
				formatMessageTag( command_tag, warning_count),
				routine,
				"Error processing crop pattern time series " +
				"for \"" + culoc_id + "\"" );
			}
			else {	Message.printWarning ( 2,
				formatMessageTag( command_tag, warning_count),
				routine,
				"Error processing sprinkler acreage time " +
				"series for \"" + culoc_id + "\"" );
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( 3, routine, e );
			}
		}
	}

	if ( do_patterns ) {
		// The above code edited individual values in time series.  Loop
		// through now and make sure that the totals are up to date...

		size = __CUCropPatternTS_Vector.size();
		StateCU_CropPatternTS cds;
		for (int i = 0; i < size; i++) {
			cds = (StateCU_CropPatternTS)
				__CUCropPatternTS_Vector.get(i);
			// Finally, if a crop pattern value is set in any year,
			// assume that all other missing values should be
			// treated as zero.  In other words, crop patterns for a
			// year must include all crops and filling should not
			// occur in a year when data values have been set.  This
			// is a bit of a performance hit but ensures
			// that values are set properly.
			cds.setCropAreasToZero ( -1, false );
			// Recalculate totals...
			cds.refresh();
		}

		// Warn about identifiers that have been replaced in the
		// __CUCropPatternTS_Vector...

		warnAboutDataMatches ( command, true,
			__CUCropPatternTS_match_Vector,
			"CU Crop Pattern TS values" );
	}

	// Check for input data that were not used.

	size = 0;
	if ( irrigsummaryts_Vector != null ) {
		size = irrigsummaryts_Vector.size();
	}
	for ( int i = 0; i < size; i++ ) {
		h_cds = (HydroBase_StructureView)
			irrigsummaryts_Vector.get(i);
		if ( !h_cds.isDirty() ) {
			Message.printWarning ( 2,
			formatMessageTag( command_tag, warning_count),
			routine, "Irrigated summary data structure id=" +
			h_cds.getStructure_id() +
			" year=" + h_cds.getCal_year() +
			" croptype=" + h_cds.getLand_use() +
			" was not matched with a CU Location - check input" );
		}
	}
	size = 0;
	if ( parcelusets_Vector != null ) {
		size = parcelusets_Vector.size();
	}
	for ( int i = 0; i < size; i++ ) {
		h_parcel = (HydroBase_ParcelUseTS)
			parcelusets_Vector.get(i);
		if ( !h_parcel.isDirty() ) {
			Message.printWarning ( 2,
			formatMessageTag( command_tag, warning_count),
			routine,
			"Well-only parcel " + h_parcel.getParcel_id() +
			" year=" + h_parcel.getCal_year() +
			" croptype=" + h_parcel.getLand_use() +
			" irrigtype=" + h_parcel.getIrrig_type() +
			" was not matched with a CU Location - check input" );
		}
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings processing the command.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new Exception ( message );
	}
}

/**
Read parcel_use_ts records and sumarize for a structure, for cases when the
HydroBase irrig_summary_ts data cannot be used.  This is implemented to simulate
GIS shapefile DBF data that was truncated to .2 precision.
*/
/* TODO SAM 2007-04-16 Don't need code.  Remove ASAP
private Vector readCropPatternTSFromHydroBaseHelper (	int wd, int id,
							int AreaPrecision_int )
{	Vector cropts_Vector = new Vector();

	// Read the parcel_use_ts vector and parcel_to_structure data for the
	// structure of interest.

	// First get the structure num...

	HydroBase_StructureView str = null;
	try {	str = __hdmi.readStructureViewForWDID ( wd, id );
	}
	catch ( Exception e ) {
		return cropts_Vector;
	}

	// Now read the data...

	Vector parcel_Vector = null;
	try {	parcel_Vector =
		__hdmi.readParcelUseTSStructureToParcelListForStructure_num (
		str.getStructure_num() );
	}
	catch ( Exception e ) {
		return cropts_Vector;
	}

	// Loop through and summarize the values..

	int size = 0;
	if ( parcel_Vector != null ) {
		size = parcel_Vector.size();
	}

	HydroBase_ParcelUseTSStructureToParcel parcel = null;	// Parcel to
								// process
	HydroBase_StructureView cropts,		// Single cropts record to
						// return
				sv = null;	// StructureView in list to
						// return - match for new data
	int cal_year, j, cropts_size;
	String crop;
	double area;	// Parcel area
	for ( int i = 0; i < size; i++ ) {
		parcel = (HydroBase_ParcelUseTSStructureToParcel)
			parcel_Vector.get(i);
		cal_year = parcel.getCal_year();
		crop = parcel.getLand_use();
		// Search through the list of records that are in memory to
		// match the year and crop...
		cropts_size = cropts_Vector.size();
		cropts = null;
		for ( j = 0; j < cropts_size; j++ ) {
			sv =(HydroBase_StructureView)cropts_Vector.get(j);
			if (	(sv.getCal_year() == cal_year) &&
				crop.equalsIgnoreCase(sv.getLand_use()) ) {
				cropts = sv;
				break;
			}
		}
		if ( cropts == null ) {
			// Was not matched so create a new one...
			cropts = new HydroBase_StructureView ();
			cropts.setWD ( wd );
			cropts.setID ( id );
			cropts.setStructure_num ( str.getStructure_num() );
			cropts.setDiv ( parcel.getDiv() );
			cropts.setLand_use ( crop );
			cropts.setCal_year ( cal_year );
			cropts.setAcres_total ( 0.0 );
			cropts_Vector.add ( cropts );
		}
		// Add the numerical information...
		if ( AreaPrecision_int > 0 ) {
			// Need to truncate the area before processing...
			area =
			((double)
			((int)(parcel.getArea()*
				Math.pow(10.0,AreaPrecision_int))))/
				Math.pow(10.0,AreaPrecision_int);

		}
		else {	area = parcel.getArea();
		}
		cropts.setAcres_total ( cropts.getAcres_total() +
			area*parcel.getPercent_irrig() );
	}

	return cropts_Vector;
}
*/

/**
Process the synchronizeIrrigationPracticeAndCropPatternTS() command:
<pre>
synchronizeIrrigationPracticeAndCropPatternTS(ID="X",GWOnlyGWAcreage=X,
DivAndWellGWAcreage=X,SprinklerAcreage=X)
</pre>
This command synchronizes the total acreage in the irrigation practice time
series with that in the crop pattern time series, using the groundwater acreage
to adjust the total and sprinkler acreage if necessary.
@param command_tag Command tag used for messaging.
@param command Command to process.
*/
/*
private void do_synchronizeIrrigationPracticeAndCropPatternTS(
					String command_tag, String command)
throws Exception
{	String routine = "StateDMI_Processor." +
		"synchronizeIrrigationPracticeAndCropPatternTS", message;
	int warning_count = 0;
	Vector tokens = StringUtil.breakStringList ( command,
		"()", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || tokens.size() < 2 ) {
		message = "Bad command \"" + command +
		"\".  Expecting 1+ parameters.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}

	if ( (__OutputStart_DateTime == null) || (__OutputEnd_DateTime == null) ) {
		// The output period is necessary to iterate through the time
		// series...
		message = "The output period must be defined before " +
		"synchronizing time series.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
	}
	if ( __CUIrrigationPracticeTS_Vector.size() == 0 ) {
		// Irrigation practice time series are necessary for this
		// command...
		message =
		"Irrigation practice time series must be available for this "+
		"command.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
	}
	if ( __CUCropPatternTS_Vector.size() == 0 ) {
		// Crop pattern time series are necessary for this command...
		message =
		"Crop pattern time series must be available for this command.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
	}

	// Get the input needed to process the file...
	PropList props = PropList.parse (
		(String)tokens.get(1), routine, "," );
	String ID = props.getValue ( "ID" );
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

	String SynchronizeMethod = props.getValue ( "SynchronizeMethod" );
	String GWOnlyGWAcreage = props.getValue ( "GWOnlyGWAcreage" );
	String DivAndWellGWAcreage = props.getValue ( "DivAndWellGWAcreage" );
	String SprinklerAcreage = props.getValue ( "SprinklerAcreage" );

	// Possible string values for parameters...
	
	String ProratePartsToCropPatternTotal = "ProratePartsToCropPatternTotal";
	String SpecificChecks = "SpecificChecks";

	String AdjustNone = "AdjustNone";
	String AdjustGWAcreageToCropPatternTotal =
		"AdjustGWAcreageToCropPatternTotal";
	String AdjustCropPatternTotalToGWAcreage =
		"AdjustCropPatternTotalToGWAcreage";
	String AdjustGWAcreageDownToCropPatternTotal =
		"AdjustGWAcreageDownToCropPatternTotal";
	String AdjustSprinklerAcreageDownToCropPatternTotal =
		"AdjustSprinklerAcreageDownToCropPatternTotal";

	// Integer values to increase performance during loops...

	int AdjustNone_int = 0;
	int AdjustGWAcreageToCropPatternTotal_int = 1;
	int AdjustCropPatternTotalToGWAcreage_int = 2;
	int AdjustGWAcreageDownToCropPatternTotal_int = 3;
	int AdjustSprinklerAcreageDownToCropPatternTotal_int = 4;

	// Set defaults if not specified...
	int GWOnlyGWAcreage_int = 0;
	int DivAndWellGWAcreage_int = 0;
	int SprinklerAcreage_int = 0;
	if ( (SynchronizeMethod == null) ||
			SynchronizeMethod.equalsIgnoreCase(SpecificChecks) ) {
		SynchronizeMethod = SpecificChecks;	// Default
	}
	else if ( SynchronizeMethod.equalsIgnoreCase(ProratePartsToCropPatternTotal)) {
		SynchronizeMethod = ProratePartsToCropPatternTotal;
	}
	else {
		message="The SynchronizeMethod parameter \"" +
		SynchronizeMethod + "\" value is invalid.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
	}
	if ( GWOnlyGWAcreage == null ) {
		GWOnlyGWAcreage = AdjustGWAcreageDownToCropPatternTotal;
	}
	if ( DivAndWellGWAcreage == null ) {
		DivAndWellGWAcreage = AdjustGWAcreageDownToCropPatternTotal;
	}
	if ( SprinklerAcreage == null ) {
		SprinklerAcreage = AdjustSprinklerAcreageDownToCropPatternTotal;
	}
	// Check for valid values and assign integer equivalents...
	if ( GWOnlyGWAcreage.equalsIgnoreCase(AdjustNone) ) {
		GWOnlyGWAcreage_int = AdjustNone_int;
	}
	else if ( GWOnlyGWAcreage.equalsIgnoreCase(
		AdjustGWAcreageToCropPatternTotal) ) {
		GWOnlyGWAcreage_int = AdjustGWAcreageToCropPatternTotal_int;
	}
	else if ( GWOnlyGWAcreage.equalsIgnoreCase(
			AdjustGWAcreageDownToCropPatternTotal) ) {
			GWOnlyGWAcreage_int = AdjustGWAcreageDownToCropPatternTotal_int;
		}
	else if ( GWOnlyGWAcreage.equalsIgnoreCase(
		AdjustCropPatternTotalToGWAcreage) ) {
		GWOnlyGWAcreage_int = AdjustCropPatternTotalToGWAcreage_int;
	}
	else {	message =
		"The GWOnlyGWAcreage parameter \"" +
		GWOnlyGWAcreage + "\" value is invalid.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
	}
	if ( DivAndWellGWAcreage.equalsIgnoreCase(AdjustNone) ) {
		DivAndWellGWAcreage_int = AdjustNone_int;
	}
	else if ( DivAndWellGWAcreage.equalsIgnoreCase(
		AdjustGWAcreageDownToCropPatternTotal) ) {
		DivAndWellGWAcreage_int =
		AdjustGWAcreageDownToCropPatternTotal_int;
	}
	else {	message =
		"The DivAndWellGWAcreage parameter \"" +
		DivAndWellGWAcreage + "\" value is invalid.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
	}
	if ( SprinklerAcreage.equalsIgnoreCase(AdjustNone) ) {
		SprinklerAcreage_int = AdjustNone_int;
	}
	else if ( SprinklerAcreage.equalsIgnoreCase(
		AdjustSprinklerAcreageDownToCropPatternTotal) ) {
		SprinklerAcreage_int =
		AdjustSprinklerAcreageDownToCropPatternTotal_int;
	}
	else {	message =
		"The SprinklerAcreage parameter \"" +
		SprinklerAcreage + "\" value is invalid.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag,++warning_count),
		routine, message );
	}

	// Throw a warning if input was not valid...
	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings checking command parameters.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new Exception ( message );
	}

	int size = 0;
	if ( __CULocation_Vector != null ) {
		size = __CULocation_Vector.size();
	}
	StateCU_Location culoc = null;		// Location to process
	StateCU_IrrigationPracticeTS ipy = null;// Irrigation practice TS
	StateCU_CropPatternTS cds = null;	// Crop pattern ts
	String id;
	int pos = 0;
	TS ts_ipy_total = null;		// IPY total acres time series.
	TS ts_ipy_gw = null;		// IPY groundwater acres time series.
	TS ts_ipy_sprinkler = null;	// IPY sprinkler acres time series.
	TS ts_ipy_gw_sprinkler = null;	// IPY GW sprinkler acres time series.
	TS ts_ipy_gw_flood = null;	// IPY GW flood acres time series.
	TS ts_ipy_sw_sprinkler = null;	// IPY SW sprinkler acres time series.
	TS ts_ipy_sw_flood = null;	// IPY SW flood acres time series.
	boolean is_gw_only = false;
				// Whether the location is ground-water only.
	int year1 = __OutputStart_DateTime.getYear();	// Years for iteration.
	int year2 = __OutputEnd_DateTime.getYear();
	int ipy_total = 0;	// Total acres from the IPY, as integer
	double ipy_total_double = 0.0;
	int ipy_gw = 0;		// Groundwater acres from the IPY, as integer
	double ipy_gw_double = 0.0;
	int ipy_sprinkler = 0;	// Sprinkler acres from the IPY, as integer
	double ipy_sprinkler_double = 0.0;
	int ipy_gw_sprinkler = 0;	// GW Sprinkler acres from the IPY, as integer
	double ipy_gw_sprinkler_double = 0.0;
	int ipy_gw_flood = 0;	// GW Flood acres from the IPY, as integer
	double ipy_gw_flood_double = 0.0;
	int ipy_sw_sprinkler = 0;	// SW Sprinkler acres from the IPY, as integer
	double ipy_sw_sprinkler_double = 0.0;
	int ipy_sw_flood = 0;	// SW Flood acres from the IPY, as integer
	double ipy_sw_flood_double = 0.0;
	int cds_total = 0;	// Total acres from the CDS, as integer
	double cds_total_double = 0.0;
	DateTime date = new DateTime ( DateTime.PRECISION_YEAR );
				// Used to iterate through time series.
	int year = 0;		// Year used to iterate
	for (int i = 0; i < size; i++) {
		culoc = (StateCU_Location)__CULocation_Vector.get(i);
		id = culoc.getID();
		if ( !id.matches(idpattern_Java) ) {
			// Identifier does not match...
			continue;
		}
		// Have a match so synchronize the TS...
		Message.printStatus ( 2, routine,
		"Synchronizing irrigation practice and crop pattern time " +
			"series for " + id );
		// Get the irrigation practice time series (contains time
		// series for each data type in the file)...
		pos = StateCU_Util.indexOf(__CUIrrigationPracticeTS_Vector,id);
		if ( pos >= 0 ) {
			// Get the time series...
			ipy = (StateCU_IrrigationPracticeTS)
				__CUIrrigationPracticeTS_Vector.
				get(pos);
		}
		if ( (pos < 0) || (ipy == null) ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine,
			"Unable to find irrigation practice time series for \""+
			id + "\".  Not synchronizing." );
			continue;
		}
		// Get the total acres time series from the irrigation
		// practice...
		ts_ipy_total = ipy.getTacreTS();
		if ( ts_ipy_total == null ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count),
			routine,
			"Unable to find irrigation practice total acres " +
			"time series for \""+ id + "\".  Not synchronizing." );
			continue;
		}
		// Get the groundwater acres time series from the irrigation
		// practice...
		ts_ipy_gw = ipy.getGacreTS();
		if ( ts_ipy_gw == null ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Unable to find irrigation practice groundwater acres "+
			"time series for \""+ id + "\".  Not synchronizing." );
			continue;
		}
		// Get the sprinkler acres time series from the irrigation
		// practice...
		ts_ipy_sprinkler = ipy.getSacreTS();
		if ( ts_ipy_sprinkler == null ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Unable to find irrigation practice sprinkler acres " +
			"time series for \""+ id + "\".  Not synchronizing." );
			continue;
		}
		// Get the GW sprinkler acres time series from the irrigation
		// practice...
		ts_ipy_gw_sprinkler = ipy.getAcgwsprTS();
		if ( ts_ipy_gw_sprinkler == null ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Unable to find irrigation practice GW sprinkler acres " +
			"time series for \""+ id + "\".  Not synchronizing." );
			continue;
		}
		// Get the GW flood acres time series from the irrigation
		// practice...
		ts_ipy_gw_flood = ipy.getAcgwflTS();
		if ( ts_ipy_gw_flood == null ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Unable to find irrigation practice GW flood acres " +
			"time series for \""+ id + "\".  Not synchronizing." );
			continue;
		}
		// Get the SW sprinkler acres time series from the irrigation
		// practice...
		ts_ipy_sw_sprinkler = ipy.getAcswsprTS();
		if ( ts_ipy_sw_sprinkler == null ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Unable to find irrigation practice SW sprinkler acres " +
			"time series for \""+ id + "\".  Not synchronizing." );
			continue;
		}
		// Get the SW flood acres time series from the irrigation
		// practice...
		ts_ipy_sw_flood = ipy.getAcswflTS();
		if ( ts_ipy_sw_flood == null ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Unable to find irrigation practice SW flood acres " +
			"time series for \""+ id + "\".  Not synchronizing." );
			continue;
		}
		// Get the crop pattern time series (contains more than one data
		// type)...
		pos = StateCU_Util.indexOf( __CUCropPatternTS_Vector, id );
		if ( pos >= 0 ) {
			// Get the time series...
			cds = (StateCU_CropPatternTS)
				__CUCropPatternTS_Vector.get(pos);
		}
		if ( (pos < 0) || (cds == null) ) {
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"Unable to find crop pattern time series for \""+
			id + "\".  Not synchronizing." );
			continue;
		}
		// Figure out if the location is a ground-water only location.
		// Currently this includes only by well aggregate/systems.
		is_gw_only = culoc.hasGroundwaterOnlySupply();
		// Loop through the years of the period for this location and
		// check the data...
		for (	year = year1, date.setYear ( year1 );
			year <= year2;
			year++, date.addYear(1) ) {
			// For the following, the IPY acres are integer values
			// whereas the CDS is floating point .3.  In order to
			// not print excessive messages about resets, do all
			// comparisons on an integer basis, using formatting
			// that would occur when writing the file (therefore it
			// is not a simple cast).  Use the doubles to
			// set the values so that the in-memory values are the
			// actual and will be formatted on output to the
			// proper precision.
			ipy_total_double = ts_ipy_total.getDataValue ( date );
			ipy_total = StringUtil.atoi(
				StringUtil.formatString(
				ipy_total_double,"%.0f"));
			ipy_gw_double = ts_ipy_gw.getDataValue ( date );
			ipy_gw = StringUtil.atoi(
				StringUtil.formatString(ipy_gw_double,"%.0f"));
			ipy_sprinkler_double =
				ts_ipy_sprinkler.getDataValue(date);
			ipy_sprinkler = StringUtil.atoi(
				StringUtil.formatString(
				ipy_sprinkler_double,"%.0f"));
			ipy_gw_flood_double =
				ts_ipy_gw_flood.getDataValue(date);
			ipy_gw_flood = StringUtil.atoi(
				StringUtil.formatString(
				ipy_gw_flood_double,"%.0f"));
			ipy_gw_sprinkler_double =
				ts_ipy_gw_sprinkler.getDataValue(date);
			ipy_gw_sprinkler = StringUtil.atoi(
				StringUtil.formatString(
				ipy_gw_sprinkler_double,"%.0f"));
			ipy_sw_flood_double =
				ts_ipy_sw_flood.getDataValue(date);
			ipy_sw_flood = StringUtil.atoi(
				StringUtil.formatString(
				ipy_sw_flood_double,"%.0f"));
			ipy_sw_sprinkler_double =
				ts_ipy_sw_sprinkler.getDataValue(date);
			ipy_sw_sprinkler = StringUtil.atoi(
				StringUtil.formatString(
				ipy_sw_sprinkler_double,"%.0f"));
			cds_total_double = cds.getTotalArea ( year );
			cds_total = StringUtil.atoi (
				StringUtil.formatString(
				cds_total_double,"%.0f"));
			Message.printStatus ( 2, routine,
			"Before sync, ID=" + id +
			" Year=" + date.getYear() +
			" GWOnly=" + is_gw_only +
			" CDS_tot=" + cds_total +
			" IPY_tot=" + ipy_total +
			" IPY_gw=" + ipy_gw +
			" IPY_gw_sprink=" + ipy_gw_sprinkler +
			" IPY_gw_flood=" + ipy_gw_flood + 
			" IPY_sw_sprink=" + ipy_sw_sprinkler +
			" IPY_sw_flood=" + ipy_sw_flood );
			if ( SynchronizeMethod.equalsIgnoreCase(ProratePartsToCropPatternTotal)) {
			}
			else {
			// The old specific checks...
			if ( is_gw_only ) {
				// The crop pattern total should be the same as
				// the ground-water only total from the
				// irrigation practice time series.  Because the
				// CDS time series are maintained internally and
				// the total is computed from the crop time
				// series, adjusting the total will result in
				// an adjustment to the acreage for each crop.
				if (	(GWOnlyGWAcreage_int ==
					AdjustCropPatternTotalToGWAcreage_int)&&
					(ipy_gw != cds_total) ) {
					Message.printStatus ( 2, routine,
					"\"" + id + "\" " + year +
					" crop pattern total acres (" +
					cds_total + ") is being reset to " +
					"irrigation practice groundwater " +
					"acres (" + ipy_gw + ")." );
					// This causes the crop acreage to
					// be prorated accordingly...
					cds.setTotalArea ( year, ipy_gw_double);
				}
				else if((GWOnlyGWAcreage_int ==
					AdjustGWAcreageToCropPatternTotal_int)&&
					(ipy_gw != cds_total) ) {
					Message.printStatus ( 2, routine,
					"\"" + id + "\" " + year +
					" groundwater acres (" +
					ipy_gw + ") is being reset to " +
					"crop pattern total acres (" +
					cds_total + ")." );
					ts_ipy_gw.setDataValue ( date,
					cds_total_double );
				}
				else if((GWOnlyGWAcreage_int ==
					AdjustGWAcreageDownToCropPatternTotal_int)&&
					(ipy_gw > cds_total) ) {
					Message.printStatus ( 2, routine,
					"\"" + id + "\" " + year +
					" groundwater acres (" +
					ipy_gw + ") is being reset DOWN to " +
					"crop pattern total acres (" +
					cds_total + ")." );
					ts_ipy_gw.setDataValue ( date,
					cds_total_double );
				}
				if (	(SprinklerAcreage_int ==
					AdjustSprinklerAcreageDownToCropPatternTotal_int)&&
					(ipy_sprinkler > cds_total) ) {
					Message.printStatus (2, routine,
					"\"" + id + "\" " + year +
					" irrigation practice sprinkler acres ("
					+ ipy_sprinkler +
					") is being reset DOWN to "+
					"CDS total (" + cds_total+").");
					ts_ipy_sprinkler.setDataValue (
					date, cds_total_double );
				}
				// The IPY total acres is probably not set so
				// set to the CDS total...
				ts_ipy_total.setDataValue(date,
					cds.getTotalArea(year));
			}
			else {	// D&W (Diversion and well location with surface
				// and groundwater supply).
				// The irrigation practice total should be set
				// to the crop pattern time series total.  Also
				// reset the groundwater and sprinkler acreage
				// if they are then larger than the total.  The
				// IPY time series are independent so the values
				// can/must be set directly (setting one will
				// not automatically adjust the others).
				//
				// In the following the CDS total is not
				// adjusted so the original value can be used to
				// set various values.  If the code changes and
				// the CDS value changes, then the new value
				// will need to be used to set values...
				if (	(DivAndWellGWAcreage_int ==
					AdjustGWAcreageDownToCropPatternTotal_int)&&
					(ipy_gw > cds_total) ) {
					Message.printStatus (2, routine,
					"\"" + id + "\" " + year +
					" irrigation practice " +
					"groundwater acres (" +
					ipy_gw +") is being reset DOWN to " +
					"CDS total (" + cds_total+ ")." );
						ts_ipy_gw.setDataValue ( date,
							cds_total_double );
				}
				if (	(SprinklerAcreage_int ==
					AdjustSprinklerAcreageDownToCropPatternTotal_int)&&
					(ipy_sprinkler > cds_total) ) {
					Message.printStatus (2, routine,
					"\"" + id + "\" " + year +
					" irrigation practice sprinkler acres ("
					+ ipy_sprinkler +
					") is being reset DOWN to "+
					"CDS total (" + cds_total+").");
					ts_ipy_sprinkler.setDataValue (
					date, cds_total_double );
				}
				if ( ipy_total != cds_total ) {
					/ * TODO SAM 2005-03-23 probably no
					need for the warning since the IPY value
					is probably missing...
					Message.printStatus ( 2, routine,
					"\"" + id + "\" " + year +
					" irrigation practice total acres (" +
					ipy_total + ") is being reset to crop "+
					"pattern total (" + cds_total + ").");
					* /
					ts_ipy_total.setDataValue ( date,
						cds_total_double );
				}
			}
			} // End SynchronizeMethod
		}
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings processing the command.";
		Message.printWarning ( 2,
		formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new Exception ( message );
	}
}
*/

/**
Fill a time series using a pattern.  This is a utility method to help with
several commands, to avoid duplicate code.
@param command_tag Command tag used for messaging.
@param warning_count Warning count used for messaging.
@param ts Monthly time series to fill.
@param routine The calling routine name, used for output messages.
@param id Identifier of the time series (station) to fill.
@param part_id Identifier of the time series part (station) to fill.  If
specified as null, it will not be printed to output.
@param data_type Data type to use in messages.
@param FillStart_DateTime Start date for filling.
@param FillEnd_DateTime End date for filling.
@param props properties used when filling.  Set FillFlag to a single character to flag the data when filling.
@return the updated warning count.
@exception Exception if there is an error finding the pattern time series.
*/
protected int fillTSMonthlyAverage ( String command_tag, int warning_count,
	MonthTS ts, String routine, String id, String part_id, String data_type,
	DateTime FillStart_DateTime, DateTime FillEnd_DateTime, PropList props )
throws Exception
{	MonthTSLimits average_limits = (MonthTSLimits)ts.getDataLimitsOriginal();
	// Usually want to use the location ID.  If it is not available, use what is in the time series...
	if ( (id == null) || id.equals("") ) {
		id = ts.getIdentifier().getLocation();
	}
	if ( average_limits == null ) {
		// Before warning, check whether there is actually any missing data.  Otherwise, the warning is
		// generated for no reason and confuses users
		int nMissing = TSUtil.missingCount(ts, FillStart_DateTime, FillEnd_DateTime);
		if ( nMissing > 0 ) {
			// Go ahead and warn
			String message = null;
			if ( part_id == null ) {
				message = "Unable to get average limits for \"" + id + "\".  Entire time series is missing.";
				Message.printWarning ( 2, formatMessageTag(command_tag,++warning_count), routine, message );
			}
			else {
				message = "Unable to get average limits for \"" + id +
				"\" (part " + part_id + ").  Entire time series is missing.";
				Message.printWarning ( 2, formatMessageTag(command_tag,++warning_count), routine, message );
			}
			ts.addToGenesis( "Attempted to fill with historical averages but no historical averages were available.");
			throw new Exception ( message );
		}
	}
	else {
		String nl = System.getProperty ( "line.separator" );
		if ( part_id != null ) {
			Message.printStatus ( 2, routine, "Filling missing data in " + id + " (part " + part_id +
				") diversion TS with monthly averages:" + nl + average_limits.toString() );
		}
		else {
			Message.printStatus ( 2, routine, "Filling missing data in " + id +
				" diversion TS with monthly averages:" + nl + average_limits.toString() );
		}
		String FillFlag = props.getValue("FillFlag");
		int nFilled = TSUtil.fillConstantByMonth ( ts, FillStart_DateTime,
			FillEnd_DateTime, average_limits.getMeanArray(), ", fill w/ hist mon ave", FillFlag, null );
	}
	return warning_count;
}

/**
Fill a time series using a pattern.  This is a utility method to help with
several commands, to avoid duplicate code.
@param ts Time series to fill.
@param routine The calling routine name, used for output messages.
@param PatternID The pattern identifier to use for filling.
@param id Identifier of the time series (station) to fill.
@param part_id Identifier of the time series part (station) to fill.  If
specified as null, it will not be printed to output.
@param data_type Data type to use in messages.
@param FillStart_DateTime Start date for filling.
@param FillEnd_DateTime End date for filling.
@param fillprops Properties for TSUtil.fillTSPattern().
@exception Exception if there is an error finding the pattern time series.
*/
protected int fillTSPattern ( TS ts, String routine, String PatternID, String id, String part_id,
	String data_type, DateTime FillStart_DateTime, DateTime FillEnd_DateTime, PropList fillprops,
	int warningLevel, int warningCount, String commandTag, CommandStatus status )
throws Exception
{	if ( part_id == null ) {
		Message.printStatus ( 2, routine, "Filling missing data in " +
		id + " " + data_type + " TS with pattern averages using \"" + PatternID + "\"" );
	}
	else {
		Message.printStatus ( 2, routine, "Filling missing data in " +
		id + " (part " + part_id + ") " + data_type +
		" TS with pattern averages using \"" + PatternID + "\"" );
	}
	// Get the pattern time series to use...
	StringMonthTS patternts = lookupFillPatternTS(PatternID);
	if ( patternts == null ) {
		if ( part_id == null ) {
			String message = "Unable to find pattern time series \"" + PatternID +
			"\" to fill \"" + ts.getIdentifierString() + "\"";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
			throw new Exception ( message );
		}
		else {
			String message = "Unable to find pattern time series \"" + PatternID +
			"\" to fill \"" + ts.getIdentifierString() + "\" (part " + part_id + ")";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
			throw new Exception ( message );
		}
	}
	TSUtil.fillPattern ( ts, patternts, FillStart_DateTime, FillEnd_DateTime, null, fillprops );
	return warningCount;
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	__gui = null;
	__commandFilename = null;
	__commandList = null;
	__OutputStart_DateTime = null;
	__OutputEnd_DateTime = null;
	__CUBlaneyCriddle_Vector = null;
	__CUCropCharacteristics_Vector = null;
	__CUCropPatternTS_Vector = null;
	//__HydroBase_Supplemental_StructureIrrigSummaryTS_Vector = null;
	__HydroBase_Supplemental_ParcelUseTS_Vector = null;
	__CUIrrigationPracticeTS_Vector = null;
	__CULocation_Vector = null;
	__CUClimateStation_Vector = null;
	__CUClimateStation_match_Vector = null;
	__CUDelayTableAssignment_match_Vector = null;
	__CULocation_match_Vector = null;
	__CUIrrigationPracticeTS_match_Vector = null;
	__CUPenmanMonteith_Vector = null;
	__CUPenmanMonteith_match_Vector = null;
	__SMDelayTableMonthlyList = null;
	__SMDelayTableMonthly_match_Vector = null;
	__SMDelayTableDailyList = null;
	__SMDelayTableDaily_match_Vector = null;
	__SMDiversionStationList = null;
	__SMDiversion_match_Vector = null;
	__SMDiversionRightList = null;
	__SMDiversionRight_match_Vector = null;
	__SMDiversionTSMonthlyList = null;
	__SMDiversionTSMonthly_match_Vector = null;
	__SMDiversionTSMonthly2List = null;
	__SMConsumptiveWaterRequirementTSMonthlyList = null;
	__SMConsumptiveWaterRequirementTSMonthly_match_Vector = null;
	__SMDemandTSMonthlyList = null;
	__SMDemandTSMonthly_match_Vector = null;
	__SMPatternTSMonthlyList = null;
	__SMDiversionTSDailyList = null;
	// TODO SAM 2007-02-18 Enable if needed
	//__SMDiversionTSDaily_match_Vector = null;
	__SM_network = null;
	__SMReservoirStationList = null;
	__SMReservoirRightList = null;
	__SMReservoir_match_Vector = null;
	__SMReservoirRight_match_Vector = null;
	__SMInstreamFlowStationList = null;
	__SMInstreamFlowRightList = null;
	__SMInstreamFlow_match_Vector = null;
	__SMInstreamFlowDemandTSAverageMonthlyList = null;
	// TODO SAM 2007-02-18 Enable if needed.
	//__SMInstreamFlowDemandTSAverageMonthly_match_Vector = null;
	__SMStreamGageStationList = null;
	__SMStreamGage_match_Vector = null;
	__SMStreamEstimateStationList = null;
	__SMStreamEstimate_match_Vector = null;
	__SMStreamEstimateCoefficients_Vector = null;
	__SMStreamEstimateCoefficients_match_Vector = null;
	__SMPrfGageData_Vector = null;
	__SMPrfGageData_match_Vector = null;
	__SMWellList = null;
	__SMWellRightList = null;
	__SMWell_match_Vector = null;
	__SMWellRight_match_Vector = null;
	__SMWellHistoricalPumpingTSMonthlyList = null;
	__SMWellDemandTSMonthlyList = null;
	__SMRiverNetworkNode_Vector = null;
	__SMRiverNetworkNode_match_Vector = null;
	__SMOperationalRight_match_Vector = null;
	__hdmi = null;
	__listeners = null;
	super.finalize();
}

/**
Find an AgStats time series using the location (county) and data type (crop).
@return the found time series or null if not found.
@param county County to find.
@param crop_type Crop type to find.
*/
protected YearTS findAgStatsTS ( String county, String crop_type )
{	int size = 0;
	if ( __CUAgStatsTS_Vector != null ) {
		size = __CUAgStatsTS_Vector.size();
	}
	YearTS ts;
	for ( int i = 0; i < size; i++ ) {
		ts = (YearTS)__CUAgStatsTS_Vector.get(i);
		if ( ts.getLocation().equalsIgnoreCase(county) && ts.getDataType().equalsIgnoreCase(crop_type) ) {
			return ts;
		}
	}
	return null;
}

/**
Add a StateCU_BlaneyCriddle instance to the __CUBlaneyCriddle_Vector.  If
an existing instance is found, it is optionally replaced and added to the __CUBlaneyCriddle_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param kbc StateCU_BlaneyCriddle instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddCUBlaneyCriddle ( StateCU_BlaneyCriddle kbc, boolean replace )
{	String id = kbc.getName();

	int pos = StateCU_Util.indexOfName ( __CUBlaneyCriddle_Vector, kbc.getName() );
	if ( pos >= 0 ) {
		// The StateCU_BlaneyCriddle is already in the list...
		__CUBlaneyCriddle_match_Vector.add(id);
		if ( replace ) {
			__CUBlaneyCriddle_Vector.set ( pos, kbc );
		}
	}
	else {
		// Add at the end of the list...
		__CUBlaneyCriddle_Vector.add ( kbc );
	}
}

/**
Add a StateCU_ClimateStation instance to the __CUClimateStations_Vector.  If an
existing instance is found, it is optionally replaced and added to the
__CUClimateStations_match_Vector so that a warning can be printed using warnAboutDataMatches().
@param cli CUClimateStation instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddCUClimateStation ( StateCU_ClimateStation cli, boolean replace )
{	String id = cli.getID();
	int pos = StateCU_Util.indexOf(__CUClimateStation_Vector,id);
	if ( pos >= 0 ) {
		// The StateCU_ClimateStation is already in the list...
		__CUClimateStation_match_Vector.add(id);
		if ( replace ) {
			__CUClimateStation_Vector.set ( pos, cli );
		}
	}
	else {
		// Add at the end of the list...
		__CUClimateStation_Vector.add ( cli );
	}
}

/**
Add a StateCU_CropCharacteristics instance to the __CUCropCharacteristics_Vector.  If
an existing instance is found, it is optionally replaced and added to the __CUCropCharacteristics_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param cch StateCU_CropCharacteristics instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddCUCropCharacteristics ( StateCU_CropCharacteristics cch, boolean replace )
{	String id = cch.getName();	// Name is used because ID is the numeridal crop ID that is no longer used.

	int pos = StateCU_Util.indexOfName(__CUCropCharacteristics_Vector, id );
	if ( pos >= 0 ) {
		// The StateCU_CropCharacteristics is already in the list...
		__CUCropCharacteristics_match_Vector.add(id);
		if ( replace ) {
			__CUCropCharacteristics_Vector.set ( pos, cch );
		}
	}
	else {
		// Add at the end of the list...
		__CUCropCharacteristics_Vector.add ( cch );
	}
}

/**
Add StateCU_CropPatternTS data to the __CUCropPatternTS_Vector.  If
an existing instance is found, it is optionally replaced and added to the
__CUCropPatternTS_match_Vector so that a warning can be printed using
warnAboutDataMatches().  This version is used when reading individual ID/Crop/Year
data from HydroBase or other sources, where single values are found.
The parcel data are also stored with the time series, for later data filling.
@param id CULocation identifier.
@param part_id The source ID for the data, which may be the same location or
part of a collection.  If null or blank is passed, the main "id" is used for messages.
@param cal_year Year for crop data.
@param parcel_id Parcel identifier for the calendear year.
@param crop_name Name of the crop.
@param total_acres Total acres for the crop (all irrigation methods).
@param datetime1 Start of period for crops - used if a new time series is added.
@param datetime2 End of period for crops - used if a new time series is added.
@param replace If 0, an existing data value is replaced if found.  If -1,
the original instance is used.  If 1, the value is added to the previous value (this is used for aggregates).
@return the time series that was modified.
*/
protected StateCU_CropPatternTS findAndAddCUCropPatternTSValue ( String id, String part_id,	int cal_year,
	int parcel_id, String crop_name, double total_acres, DateTime datetime1, DateTime datetime2,
	String units, int replace ) // TODO SAM 2009-02-12 Evaluate , String irrigationMethod, String supplyType )
{	// First see if there is a matching instance for the CU location...
	int pos = StateCU_Util.indexOf ( __CUCropPatternTS_Vector, id);
	int dl = 1;
	StateCU_CropPatternTS cds = null;
	YearTS yts = null;
	if ( (part_id == null) || (part_id.length() == 0) ) {
		// No parts, use the main identifier...
		part_id = id;
	}
	else {
		part_id = "part:" + part_id;
	}
	if ( pos < 0 ) {
		// Add at the end of the list...
		cds = new StateCU_CropPatternTS ( id, datetime1, datetime2, units );
		__CUCropPatternTS_Vector.add ( cds );
	}
	else {
		cds = (StateCU_CropPatternTS)__CUCropPatternTS_Vector.get(pos);
	}
	// The StateCU_CropPatternTS is in the list.  Now check to see if the
	// crop is in the list of time series...
	yts = cds.getCropPatternTS ( crop_name );
	if ( yts == null ) {
		// Add the crop time series...
		yts = cds.addTS ( crop_name, true );
	}
	// Now check to see if there is an existing value...
	__temp_DateTime.setYear ( cal_year );
	double val = yts.getDataValue ( __temp_DateTime );
	boolean do_store_parcel = true;	// Whether to store raw parcel data
	if ( yts.isDataMissing(val) ) {
		// Value is missing so set...
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, "", "Initializing " + id + " from " + part_id + " " +
			cal_year + " " + crop_name + " to " + StringUtil.formatString(total_acres,"%.4f") );
		}
		yts.setDataValue ( __temp_DateTime, total_acres );
	}
	else {
		// Value is not missing.  Need to either set or add to it...
		__CUCropPatternTS_match_Vector.add ( id + "-" + cal_year + "-" + crop_name );
		if ( replace == 0 ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, "", "Replacing " + id + " from " + part_id + " " +
				cal_year + " " + crop_name + " with " + StringUtil.formatString(total_acres,"%.4f") );
			}
			yts.setDataValue ( __temp_DateTime, total_acres );
			do_store_parcel = false;
			// FIXME SAM 2007-05-18 Evaluate whether need to save observations.
		}
		else if ( replace == 1 ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, "", "Adding " + id + " from " + part_id + " " +
				cal_year + " " + crop_name + " + " + total_acres + " = " +
				StringUtil.formatString( (val + total_acres), "%.4f") );
			}
			yts.setDataValue ( __temp_DateTime, val + total_acres );
		}
	}
	if ( do_store_parcel ) {
		// Save the parcel information so that it can be used to fill later with water rights.
		StateCU_Parcel parcel = new StateCU_Parcel ();
		parcel.setID( "" + parcel_id );
		parcel.setYear( cal_year );
		parcel.setCrop( crop_name );
		parcel.setArea( total_acres );
		parcel.setAreaUnits ( units );
		/* TODO SAM 2009-02-12 Evaluate whether needed - may have mutiple supply types
		if ( irrigationMethod != null ) {
			parcel.setIrrigationMethod ( irrigationMethod );
		}
		if ( supplyType != null ) {
			parcel.setSupplyType ( supplyType );
		}
		*/
		cds.addParcel ( parcel );
	}
	return cds;
}

/**
Add a StateCU_CropPatternTS instance to the __CUCropPatternTS_Vector.  If
an existing instance is found, it is optionally replaced and added to the __CUCropPatternTS_match_Vector
so that a warning can be printed using warnAboutDataMatches().
This version is used when reading data from StateCU files, where entire periods of crop data are found.
@param cch StateCU_CropPatternTS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddCUCropPatternTS ( StateCU_CropPatternTS cds, boolean replace )
{	String id = cds.getID();

	int pos = StateCU_Util.indexOf ( __CUCropPatternTS_Vector, id);
	if ( pos >= 0 ) {
		// The StateCU_CropPatternTS is already in the list...
		__CUCropPatternTS_match_Vector.add(id);
		if ( replace ) {
			__CUCropPatternTS_Vector.set ( pos, cds );
		}
	}
	else {
		// Add at the end of the list...
		__CUCropPatternTS_Vector.add ( cds );
	}
}

/**
Add StateCU_IrrigationPracticeTS groundwater acreage and pumping data to the __CUIrrigationPracticeTS_Vector.
If an existing instance is found, it is optionally replaced and added to the
__CUIrrigationPracticeTS_match_Vector
so that a warning can be printed using warnAboutDataMatches().
This version is used when reading individual well/parcel data from a database
or list file, where single values are found.
@param id CULocation identifier.
@param well_DateTime Earliest DateTime to apply the yield and acreage.
@param gw_acres Groundwater acres to add.
@param do_gw_acres If true, the gw_acres will be processed.  If false, the
acres will be processed only if the value is missing (this assumes that the
groundwater acreage has already been added, e.g., for parcel that has already
been considered in a ditch service area).
@param datetime1 Start of period for irrigation practice time series - used if a new time series is added.
@param datetime2 End of period for irrigation practice time series - used if a new time series is added.
@param replace if 0, set the values.  If 1, add to the values.
*/
// TODO SAM 2007-02-18 Need to review after StateCU review
/*
protected void findAndAddCUIrrigationPracticeGWTSValue (
						String id,
						DateTime well_DateTime,
						double yield,
						double gw_acres,
						boolean do_gwacres,
						DateTime datetime1,
						DateTime datetime2,
						String yeartype,
						int replace )
{	String routine =
	"StateDMI_Processor.findAndAddCUIrrigationPracticeGWTSValue";
	// First see if there is a matching instance for the CU location...
	int pos = StateCU_Util.indexOf ( __CUIrrigationPracticeTS_Vector, id);
	StateCU_IrrigationPracticeTS ipy = null;
	if ( pos < 0 ) {
		// Add at the end of the list...
		ipy = new StateCU_IrrigationPracticeTS ( id, datetime1,
			datetime2, yeartype, null );
		__CUIrrigationPracticeTS_Vector.add ( ipy );
	}
	else {	ipy = (StateCU_IrrigationPracticeTS)
			__CUIrrigationPracticeTS_Vector.get(pos);
	}
	// The StateCU_IrrigationPracticeTS is in the list.  Get the groundwater
	// acreage time series and maximum pumping...
	YearTS gyts = ipy.getGacreTS ();
	YearTS pyts = ipy.getMprateTS ();
	// Loop through time series period starting with the well_DateTime and
	// ending with datetime2...
	DateTime datetime = new DateTime ( well_DateTime );
	datetime.setPrecision(DateTime.PRECISION_YEAR);
	double gwval, pval;
	int year;
	int dl = 10;
	for ( ; datetime.lessThanOrEqualTo(datetime2); datetime.addYear(1) ) {
		year = datetime.getYear();	// reused below
		// Process the yield...
		// Check to see if there is an existing value...
		pval = pyts.getDataValue ( datetime );
		if ( pyts.isDataMissing(pval) ) {
			// Value is missing so set...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Setting " + id + " " + year +
				" max pumping to " + yield );
			}
			pyts.setDataValue ( datetime, yield );
		}
		else {	// Value is not missing...
			__CUIrrigationPracticeTS_match_Vector.add(id+"-"+year );
			if ( replace == 0 ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Setting " + id + " " + year +
					" max pumping to " + yield );
				}
				pyts.setDataValue ( datetime, yield );
			}
			else if ( replace == 1 ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Setting " + id + " " + year +
					" max pumping to " + pval + " + " +
					yield);
				}
				pyts.setDataValue (datetime,
					pval+yield);
			}
		}
		// Process the groundwater acres...
		// Check to see if there is an existing value...
		gwval = gyts.getDataValue ( datetime );
		if ( gyts.isDataMissing(gwval) ) {
			// Value is missing so set...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Setting " + id + " " + year +
				" GW acres to " + gw_acres );
			}
			gyts.setDataValue ( datetime, gw_acres );
		}
		else if ( do_gwacres ) {
			// Value is not missing and OK to process...
			__CUIrrigationPracticeTS_match_Vector.add (
			id + "-" + year );
			if ( replace == 0 ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Setting " + id + " " + year +
					" GW acres to " + gw_acres );
					gyts.setDataValue ( datetime,gw_acres );
				}
			}
			else if ( replace == 1 ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Setting " + id + " " + year +
					" GW acres to " + gwval + " + " +
					gw_acres);
				}
				gyts.setDataValue (datetime,
					gwval+ gw_acres);
			}
		}
	}
}
*/

/**
Add StateCU_IrrigationPracticeTS sprinkler acreage data to the __CUIrrigationPracticeTS_Vector.  If
an existing instance is found, it is optionally replaced and added to the
__CUIrrigationPracticeTS_match_Vector
so that a warning can be printed using warnAboutDataMatches().
This version is used when reading individual ID/Crop/Year data from a database
or list file, where single values are found.
@param id CULocation identifier.
@param year Year for crop data.
@param crop_name Name of the crop (will be "AllCrops" or similar, as passed in from calling code).
@param sprinkler_acres Sprinkler acres for the crop.
@param datetime1 Start of period for irrigation practice time series - used if a new time series is added.
@param datetime2 End of period for irrigation practice time series - used if a new time series is added.
@param replace If 0, an existing data value is replaced if found.  If -1,
the original instance is used.  If 1, the value is added to the previous value (this is used for aggregates).
*/
/* FIXME SAM 2007-10-18 Remove when code tests out.
protected void findAndAddCUIrrigationPracticeSprinklerTSValue (
						String id,
						int cal_year,
						String crop_name,
						double sprinkler_acres,
						DateTime datetime1,
						DateTime datetime2,
						String yeartype,
						int replace )
{	String routine =
	"StateDMI_Processor.findAndAddCUIrrigationPracticeSprinklerTSValue";
	// First see if there is a matching instance for the CU location...
	int pos = StateCU_Util.indexOf ( __CUIrrigationPracticeTS_Vector, id);
	StateCU_IrrigationPracticeTS ipy = null;
	YearTS yts = null;
	if ( pos < 0 ) {
		// Add at the end of the list...
		ipy = new StateCU_IrrigationPracticeTS ( id, datetime1,
			datetime2, yeartype, null );
		__CUIrrigationPracticeTS_Vector.add ( ipy );
	}
	else {	ipy = (StateCU_IrrigationPracticeTS)
			__CUIrrigationPracticeTS_Vector.get(pos);
	}
	// The StateCU_IrrigationPracticeTS is in the list.  Get the sprinkler
	// acreage time series...
	
	yts = ipy.getSacreTS ();
	// Now check to see if there is an existing value...
	__temp_DateTime.setYear ( cal_year );
	double val = yts.getDataValue ( __temp_DateTime );
	if ( yts.isDataMissing(val) ) {
		// Value is missing so set...
		Message.printStatus ( 2, routine,
		"Setting " + id + " " + cal_year +
		" sprinkler acres to " + sprinkler_acres );
		yts.setDataValue ( __temp_DateTime, sprinkler_acres );
	}
	else {	// Value is not missing...
		__CUIrrigationPracticeTS_match_Vector.add ( id + "-"+cal_year );
		if ( replace == 0 ) {
			Message.printStatus ( 2, routine,
			"Setting " + id + " " + cal_year +
			" sprinkler acres to " + sprinkler_acres );
			yts.setDataValue ( __temp_DateTime, sprinkler_acres );
		}
		else if ( replace == 1 ) {
			Message.printStatus ( 2, routine,
			"Setting " + id + " " + cal_year +
			" sprinkler acres to " + val + " + " + sprinkler_acres);
			yts.setDataValue (__temp_DateTime, val+sprinkler_acres);
		}
	}
}
*/

/**
Add a StateCU_IrrigationPracticeTS instance to the __CUIrrigationPracticeTS_Vector.  If
an existing instance is found, it is optionally replaced and added to the
__CUIrrigationPracticeTS_match_Vector
so that a warning can be printed using warnAboutDataMatches().
This version is used when reading data from StateCU files, where entire periods of parameter data are found.
@param ipy StateCU_IrrigationPracticeTS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddCUIrrigationPracticeTS ( StateCU_IrrigationPracticeTS ipy, boolean replace )
{	String id = ipy.getID();

	int pos = StateCU_Util.indexOf ( __CUIrrigationPracticeTS_Vector, id );
	if ( pos >= 0 ) {
		// The StateCU_IrrigationPracticeTS is already in the list...
		__CUIrrigationPracticeTS_match_Vector.add(id);
		if ( replace ) {
			__CUIrrigationPracticeTS_Vector.set ( pos, ipy );
		}
	}
	else {
		// Add at the end of the list...
		__CUIrrigationPracticeTS_Vector.add ( ipy );
	}
}

/**
Add a StateCU_Location instance to the __CULocation_Vector.  If an existing
instance is found, it is optionally replaced and added to the
__CULocation_match_Vector so that a warning can be printed using warnAboutDataMatches().
@param cu_loc CULocation instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddCULocation ( StateCU_Location cu_loc, boolean replace )
{	String id = cu_loc.getID();

	int pos = StateCU_Util.indexOf(__CULocation_Vector, cu_loc.getID());
	if ( pos >= 0 ) {
		// The StateCU_Location is already in the list...
		__CULocation_match_Vector.add(id);
		if ( replace ) {
			__CULocation_Vector.set ( pos, cu_loc );
		}
	}
	else {
		// Add at the end of the list...
		__CULocation_Vector.add ( cu_loc );
	}
}

/**
Add a time series instance to the __SMConsumptiveWaterRequirementTSMonthly_Vector.  If
an existing instance is found, it is optionally replaced and added to the
__SMConsumptiveWaterRequirementTSMonthly_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param ts TS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMConsumptiveWaterRequirementTSMonthly ( MonthTS ts, boolean replace )
{	String id = ts.getLocation();

	int pos = TSUtil.indexOf( __SMConsumptiveWaterRequirementTSMonthlyList, id, "Location", -1, 1 );
	if ( pos >= 0 ) {
		// The ts is already in the list...
		__SMConsumptiveWaterRequirementTSMonthly_match_Vector.add(id);
		if ( replace ) {
			__SMConsumptiveWaterRequirementTSMonthlyList.set ( pos, ts );
		}
	}
	else {
		// Add at the end of the list...
		__SMConsumptiveWaterRequirementTSMonthlyList.add ( ts );
	}
}

/**
Add a StateCU_PenmanMonteith instance to the __CUPenmanMonteith_Vector.  If
an existing instance is found, it is optionally replaced and added to the __CUPenmanMonteith_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param kpm StateCU_PenmanMonteith instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddCUPenmanMonteith ( StateCU_PenmanMonteith kpm, boolean replace )
{	String id = kpm.getName();

	int pos = StateCU_Util.indexOfName ( __CUPenmanMonteith_Vector, kpm.getName() );
	if ( pos >= 0 ) {
		// The StateCU_PenmanMonteith is already in the list...
		__CUPenmanMonteith_match_Vector.add(id);
		if ( replace ) {
			__CUPenmanMonteith_Vector.set ( pos, kpm );
		}
	}
	else {
		// Add at the end of the list...
		__CUPenmanMonteith_Vector.add ( kpm );
	}
}

/**
Add a StateMod_DelayTable instance to the __SMDelayTable_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMDelayTable_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param dly StateMod_DelayTable instance to be added.
@param interval TimeInterval.MONTH or TimeInterval.DAY.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMDelayTable ( StateMod_DelayTable dly, int interval, boolean replace )
{	String id = dly.getID();

	List<StateMod_DelayTable> delay_Vector = __SMDelayTableMonthlyList;
	List<String> match_Vector = __SMDelayTableMonthly_match_Vector;
	if ( interval == TimeInterval.DAY ) {
		delay_Vector = __SMDelayTableDailyList;
	}
	int pos = StateMod_Util.indexOf( delay_Vector, id );
	if ( pos >= 0 ) {
		// The StateMod_DelayTable is already in the list...
		match_Vector.add(id);
		if ( replace ) {
			delay_Vector.set ( pos, dly );
		}
	}
	else {
		// Add at the end of the list...
		delay_Vector.add ( dly );
	}
}

/**
Add a StateMod demand time series (monthly) instance to the __SMDemandTSMonthly_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMDemandTSMonthly_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param ts TS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMDemandTSMonthly ( MonthTS ts, boolean replace )
{	String id = ts.getLocation();

	int pos = TSUtil.indexOf( __SMDemandTSMonthlyList, id, "Location", -1, 1 );
	if ( pos >= 0 ) {
		// The ts is already in the list...
		__SMDemandTSMonthly_match_Vector.add(id);
		if ( replace ) {
			__SMDemandTSMonthlyList.set ( pos, ts );
		}
	}
	else {
		// Add at the end of the list...
		__SMDemandTSMonthlyList.add ( ts );
	}
}

/**
Add a StateMod_Diversion instance to the __SMDiversion_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMDiversion_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param div StateMod_Diversion instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMDiversion ( StateMod_Diversion div, boolean replace )
{	String id = div.getID();

	int pos = StateMod_Util.indexOf( __SMDiversionStationList, id );
	if ( pos >= 0 ) {
		// The StateMod_Diversion is already in the list...
		__SMDiversion_match_Vector.add(id);
		if ( replace ) {
			__SMDiversionStationList.set ( pos, div );
		}
	}
	else {
		// Add at the end of the list...
		__SMDiversionStationList.add ( div );
	}
}

/**
Add a StateMod_DiversionRight instance to the __SMDiversionRight_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMDiversionRight_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param divr StateMod_DiversionRight instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMDiversionRight ( StateMod_DiversionRight divr, boolean replace )
{	String id = divr.getID(), routine = "StateDMI_Processor.findAndAddSMDiversionRight";

	int pos = StateMod_Util.indexOf( __SMDiversionRightList, id );
	if ( pos >= 0 ) {
		// The StateMod_DiversionRight is already in the list...
		__SMDiversionRight_match_Vector.add(id);
		if ( replace ) {
			__SMDiversionRightList.set ( pos, divr );
		}
	}
	else {
		// Add in sorted order...
		pos = StateMod_Util.findWaterRightInsertPosition(__SMDiversionRightList, divr );
		if ( pos < 0 ) {
			// Insert at the end...
			Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
			+ divr.getID() + "\" adding at end." );
			__SMDiversionRightList.add ( divr );
		}
		else {
			// Do the insert at the given location...
			__SMDiversionRightList.add ( pos, divr );
		}
	}
}

/**
Add a StateMod diversion time series (monthly) instance to the __SMDiversionTSMonthly_Vector.  If
an existing instance is found, it is optionally replaced and added to the
__SMDiversionTSMonthly_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param ts TS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMDiversionTSMonthly ( MonthTS ts, boolean replace )
{
	if( ts == null ) {
		return;
	}
	String id = ts.getLocation();

	int pos = TSUtil.indexOf( __SMDiversionTSMonthlyList, id, "Location", -1, 1 );
	if ( pos >= 0 ) {
		// The ts is already in the list...
		__SMDiversionTSMonthly_match_Vector.add(id);
		if ( replace ) {
			__SMDiversionTSMonthlyList.set ( pos, ts );
		}
	}
	else {
		// Add at the end of the list...
		__SMDiversionTSMonthlyList.add ( ts );
	}
}

/**
Add a StateMod diversion time series (monthly) instance to the
__SMDiversionTSMonthly2_Vector.  The copy (clone) of the time series should be made before calling this method.
@param ts TS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMDiversionTSMonthly2 ( MonthTS ts, boolean replace )
{
	if ( ts == null ) {
		return;
	}
	String id = ts.getLocation();

	int pos = TSUtil.indexOf( __SMDiversionTSMonthly2List, id, "Location", -1, 1 );
	if ( pos >= 0 ) {
		// The ts is already in the list...
		if ( replace ) {
			__SMDiversionTSMonthly2List.set ( pos, ts );
		}
	}
	else {
		// Add at the end of the list...
		__SMDiversionTSMonthly2List.add ( ts );
	}
}

/**
Add a StateMod_InstreamFlow instance to the __SMInstreamFlow_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMInstreamFlow_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param ifs StateMod_InstreamFlow instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMInstreamFlow ( StateMod_InstreamFlow ifs, boolean replace )
{	String id = ifs.getID();

	int pos = StateMod_Util.indexOf( __SMInstreamFlowStationList, id );
	if ( pos >= 0 ) {
		// The StateMod_InstreamFlow is already in the list...
		__SMInstreamFlow_match_Vector.add(id);
		if ( replace ) {
			__SMInstreamFlowStationList.set ( pos, ifs );
		}
	}
	else {
		// Add at the end of the list...
		__SMInstreamFlowStationList.add ( ifs );
	}
}

/**
Add a StateMod instream flow demand time series (average monthly) instance to the
__SMInstreamFlowDemandTSAverageMonthly_Vector.  If
an existing instance is found, it is optionally replaced and added to the list
so that a warning can be printed using warnAboutDataMatches().
@param ts TS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMInstreamFlowDemandTSAverageMonthly ( MonthTS ts, boolean replace )
{	String id = ts.getLocation();

	int pos = TSUtil.indexOf( __SMInstreamFlowDemandTSAverageMonthlyList, id, "Location", -1, 1 );
	if ( pos >= 0 ) {
		// The ts is already in the list...
		__SMInstreamFlowDemandTSAverageMonthly_match_Vector.add(id);
		if ( replace ) {
			__SMInstreamFlowDemandTSAverageMonthlyList.set ( pos, ts );
		}
	}
	else {
		// Add at the end of the list...
		__SMInstreamFlowDemandTSAverageMonthlyList.add ( ts );
	}
}

/**
Add a StateMod_InstreamFlowRight instance to the __SMInstreamFlowRight_Vector.
If an existing instance is found, it is optionally replaced and added to the __SMInstreamFlowRight_match_Vector
so that a warning can be printed using warnAboutDataMatches().
If the right is not found, it is added in alphabetical order.
@param ifr StateMod_InstreamFlowRight instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMInstreamFlowRight ( StateMod_InstreamFlowRight ifr, boolean replace )
{	String id = ifr.getID();
	String routine = "StateDMI_Processor.findAndAddSMInstreamFlowRight";

	int pos = StateMod_Util.indexOf( __SMInstreamFlowRightList, id );
	if ( pos >= 0 ) {
		// The StateMod_InstreamFlowRight is already in the list...
		__SMInstreamFlowRight_match_Vector.add(id);
		if ( replace ) {
			__SMInstreamFlowRightList.set ( pos, ifr );
		}
	}
	else {
		// Add in sorted order...
		pos = StateMod_Util.findWaterRightInsertPosition( __SMInstreamFlowRightList, ifr );
		if ( pos < 0 ) {
			// Insert at the end...
			Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
			+ ifr.getID() + "\" adding at end." );
			__SMInstreamFlowRightList.add ( ifr );
		}
		else {
			// Do the insert at the given location...
			__SMInstreamFlowRightList.add (pos, ifr);
		}
	}
}

/**
Add a StateMod_OperationalRight instance to the __SMOperationalRight_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMOperationalRight_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param opr StateMod_OperationalRight instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMOperationalRight ( StateMod_OperationalRight opr, boolean replace )
{	String id = opr.getID(), routine = "StateDMI_Processor.findAndAddSMOperationalRight";

	int pos = StateMod_Util.indexOf( __SMOperationalRightList, id );
	if ( pos >= 0 ) {
		// The StateMod_OperationalRight is already in the list...
		__SMOperationalRight_match_Vector.add(id);
		if ( replace ) {
			__SMOperationalRightList.set ( pos, opr );
		}
	}
	else {
		// TODO SAM 2010-12-11 Add at end since user probably wants in a certain order
		// Add in sorted order...
		//pos = StateMod_Util.findWaterRightInsertPosition(__SMOperationalRightList, opr );
		//if ( pos < 0 ) {
			// Insert at the end...
			//Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
			//+ opr.getID() + "\" adding at end." );
			__SMOperationalRightList.add ( opr );
		//}
		//else {
			// Do the insert at the given location...
			//__SMOperationalRightList.add ( pos, opr );
		//}
	}
}

/**
Add a StateMod_Plan instance to the __SMPlan_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMPlan_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param plan StateMod_Well instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMPlan ( StateMod_Plan plan, boolean replace )
{	String id = plan.getID();

	int pos = StateMod_Util.indexOf( __SMPlanList, id );
	if ( pos >= 0 ) {
		// The StateMod_Plan is already in the list...
		__SMPlan_match_Vector.add(id);
		if ( replace ) {
			__SMPlanList.set ( pos, plan );
		}
	}
	else {
		// Add at the end of the list...
		__SMPlanList.add ( plan );
	}
}

// TODO SAM 2011-01-02 Add based on muliple identifiers
/**
Add a StateMod_Plan_WellAugmentation instance to the __SMPlanWellAugmentation_Vector.  Currently this always
adds at the end.
@param wellAug StateMod_Plan_WellAugmentation instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMPlanWellAugmentation ( StateMod_Plan_WellAugmentation wellAug, boolean replace )
{	//String id = wellAug.getID();

/*
	int pos = StateMod_Util.indexOf( __SMPlanList, id );
	if ( pos >= 0 ) {
		// The StateMod_Plan is already in the list...
		__SMPlan_match_Vector.add(id);
		if ( replace ) {
			__SMPlanList.set ( pos, wellAug );
		}
	}
	else {*/
		// Add at the end of the list...
		__SMPlanWellAugmentationList.add ( wellAug );
	//}
}

//TODO SAM 2011-01-02 Add based on muliple identifiers
/**
Add a StateMod_ReturnFlow instance to the __SMPlanReturn_Vector.  Currently this always
adds at the end.
@param planReturn StateMod_ReturnFlow instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMPlanReturn ( StateMod_ReturnFlow planReturn, boolean replace )
{	//String id = wellAug.getID();

/*
	int pos = StateMod_Util.indexOf( __SMPlanList, id );
	if ( pos >= 0 ) {
		// The StateMod_Plan is already in the list...
		__SMPlan_match_Vector.add(id);
		if ( replace ) {
			__SMPlanList.set ( pos, wellAug );
		}
	}
	else {*/
		// Add at the end of the list...
		__SMPlanReturnList.add ( planReturn );
	//}
}

/**
Add a StateMod_PrfGageData instance to the __SMPrfGageData_Vector.  If an
existing instance is found, it is optionally replaced and added to the
__SMPrfGageData_match_Vector so that a warning can be printed using warnAboutDataMatches().
@param prf StateMod_PrfGageData instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
@return true if the location was matched, false if not.
*/
protected boolean findAndAddSMPrfGageData ( StateMod_PrfGageData prf, boolean replace )
{	String id = prf.getID();

	int pos=StateMod_Util.indexOf(__SMPrfGageData_Vector,id);
	if ( pos >= 0 ) {
		// The StateMod_PrfGageData is already in the list...
		__SMPrfGageData_match_Vector.add(id);
		if ( replace ) {
			__SMPrfGageData_Vector.set ( pos, prf );
		}
		return true;
	}
	else {
		// Add at the end of the list...
		__SMPrfGageData_Vector.add ( prf );
		return false;
	}
}

/**
Add a StateMod_Reservoir instance to the __SMReservoir_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMReservoir_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param div StateMod_Reservoir instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMReservoir ( StateMod_Reservoir res, boolean replace )
{	String id = res.getID();

	int pos = StateMod_Util.indexOf( __SMReservoirStationList, id );
	if ( pos >= 0 ) {
		// The StateMod_Reservoir is already in the list...
		__SMReservoir_match_Vector.add(id);
		if ( replace ) {
			__SMReservoirStationList.set ( pos, res );
		}
	}
	else {
		// Add at the end of the list...
		__SMReservoirStationList.add ( res );
	}
}

//TODO SAM 2011-01-02 Add based on muliple identifiers
/**
Add a StateMod_ReturnFlow instance to the __SMReservoirReturn_Vector.  Currently this always
adds at the end.
@param resReturn StateMod_ReturnFlow instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMReservoirReturn ( StateMod_ReturnFlow resReturn, boolean replace )
{	//String id = wellAug.getID();

/*
	int pos = StateMod_Util.indexOf( __SMPlanList, id );
	if ( pos >= 0 ) {
		// The StateMod_Plan is already in the list...
		__SMPlan_match_Vector.add(id);
		if ( replace ) {
			__SMPlanList.set ( pos, wellAug );
		}
	}
	else {*/
		// Add at the end of the list...
		__SMReservoirReturnList.add ( resReturn );
	//}
}

/**
Add a StateMod_ReservoirRight instance to the __SMReservoirRight_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMReservoirRight_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param resr StateMod_ReservoirRight instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMReservoirRight ( StateMod_ReservoirRight resr, boolean replace )
{	String id = resr.getID(), routine = "StateDMI_Processor.findAndAddSMReservoirRight";

	int pos = StateMod_Util.indexOf( __SMReservoirRightList, id );
	if ( pos >= 0 ) {
		// The StateMod_ReservoirRight is already in the list...
		__SMReservoirRight_match_Vector.add(id);
		if ( replace ) {
			__SMReservoirRightList.set ( pos, resr );
		}
	}
	else {
		// Add in sorted order...
		pos = StateMod_Util.findWaterRightInsertPosition(__SMReservoirRightList, resr );
		if ( pos < 0 ) {
			// Insert at the end...
			Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
			+ resr.getID() + "\" adding at end." );
			__SMReservoirRightList.add ( resr );
		}
		else {
			// Do the insert at the given location...
			__SMReservoirRightList.add ( pos, resr );
		}
	}
}

/**
Add a StateMod_RiverNetworkNode instance to the __SMRiverNetworkNode_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMRiverNetworkNode_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param rin StateMod_RiverNetworkNode instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMRiverNetworkNode (	StateMod_RiverNetworkNode rin, boolean replace )
{	String id = rin.getID();

	int pos = StateMod_Util.indexOf( __SMRiverNetworkNode_Vector, id );
	if ( pos >= 0 ) {
		// The StateMod_RiverNetworkNode is already in the list...
		__SMRiverNetworkNode_match_Vector.add(id);
		if ( replace ) {
			__SMRiverNetworkNode_Vector.set ( pos, rin );
		}
	}
	else {
		// Add at the end of the list...
		__SMRiverNetworkNode_Vector.add ( rin );
	}
}

/**
Add a StateMod_StreamEstimate instance to the __SMStreamEstimate_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMStreamEstimate_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param estimate StateMod_StreamEstimate instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMStreamEstimate (	StateMod_StreamEstimate estimate, boolean replace )
{	String id = estimate.getID();

	int pos = StateMod_Util.indexOf( __SMStreamEstimateStationList, id );
	if ( pos >= 0 ) {
		// The StateMod_StreamEstimate is already in the list...
		__SMStreamEstimate_match_Vector.add(id);
		if ( replace ) {
			__SMStreamEstimateStationList.set ( pos, estimate );
		}
	}
	else {
		// Add at the end of the list...
		__SMStreamEstimateStationList.add ( estimate );
	}
}

/**
Add a StateMod_StreamEstimate_Coefficients instance to the
__SMStreamEstimateCoefficients_Vector.  If an existing instance is found, it is
optionally replaced and added to the __SMStreamEstimateCoefficients_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param rib StateMod_StreamEstimate_Coefficients instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMStreamEstimateCoefficients ( StateMod_StreamEstimate_Coefficients rib, boolean replace )
{	String id = rib.getID();

	int pos=StateMod_Util.indexOf(__SMStreamEstimateCoefficients_Vector,id);
	if ( pos >= 0 ) {
		// The StateMod_StreamEstimate_Coefficients is already in the list...
		__SMStreamEstimateCoefficients_match_Vector.add(id);
		if ( replace ) {
			__SMStreamEstimateCoefficients_Vector.set ( pos, rib );
		}
	}
	else {
		// Add at the end of the list...
		__SMStreamEstimateCoefficients_Vector.add ( rib );
	}
}

/**
Add a StateMod_StreamGage instance to the __SMStreamGage_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMStreamGage_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param gage StateMod_StreamGage instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMStreamGage (StateMod_StreamGage gage, boolean replace )
{	String id = gage.getID();

	int pos = StateMod_Util.indexOf( __SMStreamGageStationList, id );
	if ( pos >= 0 ) {
		// The StateMod_StreamGage is already in the list...
		__SMStreamGage_match_Vector.add(id);
		if ( replace ) {
			__SMStreamGageStationList.set ( pos, gage );
		}
	}
	else {
		// Add at the end of the list...
		__SMStreamGageStationList.add ( gage );
	}
}

/**
Add a StateMod_Well instance to the __SMWell_Vector.  If
an existing instance is found, it is optionally replaced and added to the __SMWell_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param well StateMod_Well instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMWell ( StateMod_Well well, boolean replace )
{	String id = well.getID();

	int pos = StateMod_Util.indexOf( __SMWellList, id );
	if ( pos >= 0 ) {
		// The StateMod_Well is already in the list...
		__SMWell_match_Vector.add(id);
		if ( replace ) {
			__SMWellList.set ( pos, well );
		}
	}
	else {
		// Add at the end of the list...
		__SMWellList.add ( well );
	}
}

/**
Add a StateMod well demand time series (monthly) instance to the __SMWellDemandTSMonthly_Vector.  If
an existing instance is found, it is optionally replaced and added to the
__SMWellDemandTSMonthly_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param ts TS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMWellDemandTSMonthly ( MonthTS ts, boolean replace )
{	String id = ts.getLocation();

	int pos = TSUtil.indexOf( __SMWellDemandTSMonthlyList, id, "Location", -1, 1 );
	if ( pos >= 0 ) {
		// The ts is already in the list...
		__SMWellDemandTSMonthly_match_Vector.add(id);
		if ( replace ) {
			__SMWellDemandTSMonthlyList.set ( pos, ts );
		}
	}
	else {
		// Add at the end of the list...
		__SMWellDemandTSMonthlyList.add ( ts );
	}
}

/**
Add a StateMod well historical pumping time series (monthly) instance to the
__SMWellHistoricalPumpingTSMonthly_Vector.  If
an existing instance is found, it is optionally replaced and added to the
__SMWellHistoricalPumpingTSMonthly_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param ts TS instance to be added.
@param replace If true, an existing instance is replaced if found.  If false, the original instance is used.
*/
protected void findAndAddSMWellHistoricalPumpingTSMonthly(MonthTS ts, boolean replace )
{	String id = ts.getLocation();

	int pos = TSUtil.indexOf( __SMWellHistoricalPumpingTSMonthlyList, id, "Location", -1, 1 );
	if ( pos >= 0 ) {
		// The ts is already in the list...
		__SMWellHistoricalPumpingTSMonthly_match_Vector.add(id);
		if ( replace ) {
			__SMWellHistoricalPumpingTSMonthlyList.set( pos, ts );
		}
	}
	else {
		// Add at the end of the list...
		__SMWellHistoricalPumpingTSMonthlyList.add ( ts );
	}
}

/**
Find a CU Location given a ditch ID and collection information - therefore find
a well-only CU Location.  Do not combine this with findCULocationForParcel()
because the parcel_id could conceivably conflict with the ditch ID!
@return the StateCU_Location that is a collection that includes the parcel.
@param ditch_id the Ditch id to match, fully expanded WDID.
@param CULocations_Vector the Vector of StateCU_Location to search.
*/
// TODO SAM 2007-02-18 Evaluate whether needed
/*
protected StateCU_Location findCULocationForDitch ( String ditch_id, Vector CULocations_Vector )
{	// Loop through the CU Locations...

	StateCU_Location culoc = null;
	int size = 0;
	if ( CULocations_Vector != null ) {
		size = CULocations_Vector.size();
	}
	Vector partids;
	int ic;
	int collection_size;
	for ( int i = 0; i < size; i++ ) {
		culoc = (StateCU_Location)CULocations_Vector.get(i);
		if ( !culoc.isCollection() ) {
			continue;
		}
		// Currently ditches have same collection for full period...
		if ( (partids=culoc.getCollectionPartIDs(0))==null){
			continue;
		}
		// Else have a list of IDs that need to be searched...
		collection_size = partids.size();
		for ( ic = 0; ic < collection_size; ic++ ) {
			if ( ditch_id.equalsIgnoreCase(
				(String)partids.get(ic)) ) {
				// Found the matching CU Location....
				return culoc;
			}
		}
	}
	return null;
}
*/

/**
Find a CU Location given a parcel and collection information - therefore find
a well-only CU Location.  Do not combine this with findCULocationForDitch()
because the parcel_id could conceivably conflict with the ditch ID!
@return the StateCU_Location that is a collection that includes the parcel.
@param parcel_id the Parcel id to match.
@param CULocations_Vector the Vector of StateCU_Location to search.
@param div The division - used to uniquely identify the parcel.
@param parcelid_year The year to use for parcel IDs in the collection.
*/
// TODO SAM 2007-02-18 Evaluate if needed
/*
private StateCU_Location findCULocationForParcel (	int parcel_id,
						Vector CULocations_Vector,
						int div, int parcelid_year )
{	// Loop through the CU Locations...

	StateCU_Location culoc = null;
	int size = 0;
	if ( CULocations_Vector != null ) {
		size = CULocations_Vector.size();
	}
	Vector partids;
	int ic;
	int collection_size;
	String parcelid_string = "" + parcel_id;	// ID as string.
	for ( int i = 0; i < size; i++ ) {
		culoc = (StateCU_Location)CULocations_Vector.get(i);
		if ( !culoc.isCollection() ) {
			continue;
		}
		if ( culoc.getCollectionDiv() != div ) {
			continue;
		}
		if ( (partids=culoc.getCollectionPartIDs(parcelid_year))==null){
			continue;
		}
		// Else have a list of IDs that need to be searched...
		collection_size = partids.size();
		for ( ic = 0; ic < collection_size; ic++ ) {
			if ( parcelid_string.equalsIgnoreCase(
				(String)partids.get(ic)) ) {
				// Found the matching CU Location....
				return culoc;
			}
		}
	}
	return null;
}
*/

/**
Find parcel from a Vector of HydroBase_ParcelUseTS.
@return parcel from a Vector of HydroBase_ParcelUseTS, or null if not found.
@param parcelusets_Vector Vector of HydroBase_ParcelUseTS to search.
@param parcel_id Parcel id to search for.
@param div Division for data.
@param year Year to search for.
*/
// TODO SAM 2007-02-18 Review after StateCU review
/*private HydroBase_ParcelUseTS findParcelUseTS (	Vector parcelusets_Vector,
						int parcel_id,
						int div, int year )
{	if ( parcelusets_Vector == null ) {
		return null;
	}
	int size = parcelusets_Vector.size();
	HydroBase_ParcelUseTS pts = null;
	for ( int i = 0; i < size; i++ ) {
		pts = (HydroBase_ParcelUseTS)parcelusets_Vector.get(i);
		if (	(pts.getParcel_id() == parcel_id) &&
			(pts.getDiv() == div) &&
			(pts.getCal_year() == year) ) {
			return pts;
		}
	}
	return null;
}
*/

/**
Find parcels from a Vector of HydroBase_ParcelUseTS.
@return parcels from a Vector of HydroBase_ParcelUseTS.
@param parcelusets_Vector Vector of HydroBase_ParcelUseTS to search.
@param div Division for data.
@param ids_array Array of parcel identifiers to search for.
@param datetime1 Start for query.
@param datetime2 End for query.
*/
private List findParcelUseTSListForParcelList(
		List parcelusets_Vector, int div,
				int [] ids_array,
				DateTime datetime1, DateTime datetime2 )
{	List crop_patterns = new Vector();
	// For debugging...
	/*
	Vector ids_Vector = new Vector();
	if ( ids_array != null ) {
		for ( int i = 0; i < ids_array.length; i++ ) {
			ids_Vector.add ( "" + ids_array[i] );
		}
	}
	Message.printStatus ( 1, "", "Looking for parcel_use_ts for ids:" +
			ids_Vector );
	*/
	int size = 0;
	if ( parcelusets_Vector != null ) {
		size = parcelusets_Vector.size();
	}
	HydroBase_ParcelUseTS pts;
	int iid;
	int pts_id;
	int year1 = -5000;	// To simplify comparison
	if ( datetime1 != null) {
		year1 = datetime1.getYear();
	}
	int year2 = 5000;
	if ( datetime2 != null) {
		year2 = datetime2.getYear();
	}
	int pts_year;
	for ( int i = 0; i < size; i++ ) {
		pts = (HydroBase_ParcelUseTS)parcelusets_Vector.get(i);
		if ( div != pts.getDiv() ) {
			continue;
		}
		// Check to see if the year is in the requested range...
		pts_year = pts.getCal_year();
		if ( (pts_year < year1) || (pts_year > year2) ) {
			continue;
		}
		// Check to see if the parcel ID is one of the requested
		// identifiers...
		pts_id = pts.getParcel_id();
		for ( iid = 0; iid < ids_array.length; iid++ ) {
			if ( ids_array[iid] == pts_id ) {
				crop_patterns.add ( pts );
			}
		}
	}
	return crop_patterns;
}

/**
Find crop patterns from a Vector of HydroBase_StructureIrrigSummaryTS (new
is HydroBase_StructureView).
@return crop patterns from a Vector of HydroBase_StructureIrrigSummaryTS (new
is HydroBase_StructureView).
@param irrigsummaryts_Vector A Vector of HydroBase_StructureIrrigSummaryTS (new
is HydroBase_StructureView) to search.
@param culoc_wdids A Vector of identifiers for locations to match.
@param datetime1 First year to query.
@param datetime2 Last year to query.
*/
private List findStructureIrrigSummaryTSListForWDIDListLand_usePeriod
				( List irrigsummaryts_Vector,
						List culoc_wdids,
				DateTime datetime1, DateTime datetime2 )
{	List crop_patterns = new Vector ();
	int size = 0;
	if ( irrigsummaryts_Vector != null ) {
		size = irrigsummaryts_Vector.size();
	}
	HydroBase_StructureView sits;
	int iid;
	int wdids_size = 0;
	if ( culoc_wdids != null ) {
		wdids_size = culoc_wdids.size();
	}
	int year1 = -5000;	// To simplify comparison
	if ( datetime1 != null) {
		year1 = datetime1.getYear();
	}
	int year2 = 5000;
	if ( datetime2 != null) {
		year2 = datetime2.getYear();
	}
	int sits_year;
	String sits_id;
	String culoc_wdid;
	for ( int i = 0; i < size; i++ ) {
		sits = (HydroBase_StructureView)
			irrigsummaryts_Vector.get(i);
		sits_year = sits.getCal_year();
		if ( (sits_year < year1) || (sits_year > year2) ) {
			continue;
		}
		sits_id = sits.getStructure_id();
		for ( iid = 0; iid < wdids_size; iid++ ) {
			culoc_wdid = (String)culoc_wdids.get(iid);
			if ( culoc_wdid.equalsIgnoreCase(sits_id) ) {
				crop_patterns.add ( sits );
			}
		}
	}
	return crop_patterns;
}

/**
Find parcel from a Vector of HydroBase_ParcelUseTS.
@return parcel from a Vector of HydroBase_ParcelUseTS, or null if not found.
@param struct2parcel_Vector Vector of HydroBase_StructureToParcel to search.
@param parcel_id Parcel id to search for.
@param div Division for data.
@param year Year to search for.
*/
// TODO SAM 2007-02-18 Evaluate if needed
/*
private Vector findStructureToParcelListForParcel (
				Vector struct2parcel_Vector,
				int parcel_id,
				int div, int year )
{	if ( struct2parcel_Vector == null ) {
		return null;
	}
	int size = struct2parcel_Vector.size();
	HydroBase_StructureToParcel stp = null;
	Vector stp_Vector = new Vector();
	for ( int i = 0; i < size; i++ ) {
		stp = (HydroBase_StructureToParcel)
			struct2parcel_Vector.get(i);
		if (	(stp.getParcel_id() == parcel_id) &&
			(stp.getDiv() == div) &&
			(stp.getCal_year() == year) ) {
			stp_Vector.add(stp);
		}
	}
	if ( stp_Vector.size() == 0 ) {
		return null;
	}
	else {	return stp_Vector;
	}
}
*/

/**
Format a message tag for use with the Message class print methods.
The format of the string will be:
<pre>
root,count
</pre>
@param tag_root A root string to include in the tag.
@param count A count to modify the root (1+), for example, indicating the
count of warnings within a command.
*/
private String formatMessageTag ( String tag_root, int count )
{	return MessageUtil.formatMessageTag ( tag_root, count );
}

/**
Return the Command instance at the requested position.
@return The number of commands being managed by the processor
*/
public Command get( int pos )
{
	return (Command)getCommands().get(pos);
}

/**
Indicate whether cancelling processing has been requested.
@return true if cancelling has been requested.
*/
public boolean getCancelProcessingRequested ()
{
	return __cancel_processing_requested;
}

/**
Return the list of commands.
@return the list of commands.
*/
public List getCommands ()
{
	return __commandList;
}

/**
Helper method to return the current list of commands as a String.
@return Text of commands that are currently in memory.
*/
private String getCommandsAsString()
{
	String commandStr = "";
	List commandList = getCommands();
	for ( int i = 0; i < commandList.size(); i++ ) {
		commandStr += commandList.get(i).toString() + "\n";
	}
	return commandStr;
}

/**
Return the name of the command file that is being processed.
@return the name of the command file that is being processed.
*/
public String getCommandFileName ()
{	return __commandFilename;
}

/**
Returns a Vector of data for the specified application and component
@param app_type
@param comp_type
@return Vector of data based on the specified application and component
 */
protected List getComponentData ( int app_type, int comp_type ){

	if( app_type == StateDMI.APP_TYPE_STATEMOD && comp_type == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
		return __SMDiversionTSMonthlyList;
	}
	else {
		return null;
	}
}

/**
Return whether output files should be created.
@return whether output files should be created.
*/
private boolean getCreateOutput ()
{
	return __create_output;
}

/**
 * @param cmdStr - runCommand command string
 * @return runCmdFile - Input file specified by the command
 */
protected String getFileFromRunCommand(String cmdStr)
{
	String runCmdFile = "";
	String routine = "StateDMI_Processor.getFileFromRunCommand";

	//parse the command string for the InputFile
	// Split by quotes since filename should be in quotes
	runCmdFile = cmdStr.split("\"")[1];

	// Make sure we have something and if not supply message
	if(runCmdFile.equals("") || runCmdFile == null)
		Message.printWarning(1, routine, "runCommand syntax is incorrect." +
			"The correct syntax is: runCommands(InputFile=\"commands.StateDMI\")" +
			".  The corresponding command could not be run");

	return runCmdFile;
}

/**
Returns the getProgramHeader() text and HydroBase comments if HydroBase is being used.
@return The entire program header including StateDMI and
HydroBase information. 
*/
private String getFullProgramHeader()
{
	String full_header = getProgramHeader() + "\n";
	full_header += ( "Command:   " + StateDMI.PROGRAM_NAME + " " + StateDMI.getArgs() );
	if ( __hdmi != null ) {
		try {
			String [] comments = __hdmi.getVersionComments();
			for ( int i = 0; i < comments.length; i++ ) {
				if ( !comments[i].startsWith("---") ) {
					 full_header = full_header + "\n" + comments[i];
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(2, "StateDMI_Processor.runComponentChecks",
				"Couldn't get version comments from HydroBase because:\n +" +
				e.toString());
		}
	}
	return full_header;
}

/**
Returns the current HydroBaseDMI connection.
@return HydroBaseDMI HydroBaseDMI database connection.
*/
public HydroBaseDMI getHydroBaseDMIConnection()
{
	return __hdmi;
}

/**
Returns user-specified supplemental parcel use data to add to HydroBase data.
@return user-specified supplemental parcel use data to add to HydroBase data.
*/
private List getHydroBaseSupplementalParcelUseTSList ()
{
	return __HydroBase_Supplemental_ParcelUseTS_Vector;
}

/**
Return the initial working directory for the processor.
@return the initial working directory for the processor.
*/
protected String getInitialWorkingDir()
{
	return __InitialWorkingDir_String;
}

/**
Returns the header for the check file.
@return String - Header to show in the check file.
*/
private String getProgramHeader()
{
	String header = "";
	String host = "host name could not be resolved";
	header = "Program:   " + StateDMI.PROGRAM_NAME + " " +
	StateDMI.PROGRAM_VERSION + "\n" +
	"User:      " + System.getProperty("user.name").trim() + "\n" +
	"Date:      " + TimeUtil.getSystemTimeString("") + "\n";
	try {
		host = InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException e) {
		Message.printWarning(3, "StateDMI_Process.getProgramHeader",
			"Host name could not be resolved");
	}
	header = header + "Host:      " + host + "\n";
	header = header + "Directory: " + IOUtil.getProgramWorkingDir();

	return header;
}


/**
Return data for a named property, required by the CommandProcessor
interface.  See the overloaded version for a list of properties that are
handled.
@param prop Property to set.
@return the named property, or null if a value is not found.
*/
public Prop getProp ( String prop ) throws Exception
{	Object o = getPropContents ( prop );
	if ( o == null ) {
		return null;
	}
	else {	// Contents will be a Vector, etc., so convert to a full
		// property.
		// TODO SAM 2005-05-13 This will work seamlessly for strings
		// but may have a side-effects (conversions) for non-strings...
		Prop p = new Prop ( prop, o, o.toString() );
		return p;
	}
}

/**
Return the contents for a named property, required by the CommandProcessor
interface. Currently the following properties are handled:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>
</tr>

<tr>
<td><b>CreateOutput</b></td>
<td>Indicate if output should be created.  If True, commands that create output
should do so.  If False, the commands should be skipped.  This is used to
speed performance during initial testing.
</td>
</tr>

<tr>
<td><b>HydroBaseDMIList</b></td>
<td>A Vector of open HydroBaseDMI, available for reading.
Currently only one object at most will be returned.
</td>
</tr>

<tr>
<td><b>OutputYearType</b></td>
<td>The output year type.
</td>
</tr>

<tr>
<td><b>OutputEnd</b></td>
<td>The output end from the setOutputPeriod() command, as a DateTime object.
</td>
</tr>

<tr>
<td><b>OutputStart</b></td>
<td>The output start from the setOutputPeriod() command, as a DateTime object.
</td>
</tr>

<tr>
<td><b>WorkingDir</b></td>
<td>The working directory for the processor (initially the same as the
application but may be changed by commands during execution).</td>
</tr>

</table>
@return the contents for a named property, or null if a value is not found.
*/
public Object getPropContents ( String prop ) throws Exception
{
	if ( prop.equalsIgnoreCase("CreateOutput") ) {
		if ( getCreateOutput() ) {
			return new Boolean("True");
		}
		else {
			return new Boolean("False");
		}
	}
	else if ( prop.equalsIgnoreCase("CommandFileName") ) {
		return getCommandFileName();
	}
	else if ( prop.equalsIgnoreCase("CountyList") ) {
		// Get the list of counties, for use in dialogs
		List countyList = new Vector();
		if ( __hdmi != null ) {
			List countyRefList = __hdmi.getCountyRef();
			for( int i = 0; i < countyRefList.size(); i++ ) {
				countyList.add ( ((HydroBase_CountyRef)countyRefList.get(i)).getCounty() );
			}
		}
		return countyList;
	}
	else if ( prop.equalsIgnoreCase("CUMethod_List") ) {
		// Get the list of CU methods, for use in crop characteristics
		List cuMethodList = new Vector();
		if ( __hdmi != null ) {
			List hbList = __hdmi.readCUMethodList(true);
			HydroBase_CUMethod m;
			for ( int i = 0; i < hbList.size(); i++ ) {
				m = (HydroBase_CUMethod)hbList.get(i);
				cuMethodList.add ( m.getMethod_desc());
			}
		}
		return cuMethodList;
	}
	else if ( prop.equalsIgnoreCase("CUPenmanMonteithMethod_List") ) {
		// Get the list of distinct CU methods used with Penman-Montieth
		List<String> cuPenmanMonteithMethodList = new Vector();
		if ( __hdmi != null ) {
			List<HydroBase_CUPenmanMonteith> hbList = __hdmi.getPenmanMonteithCUMethod();
			for ( HydroBase_CUPenmanMonteith pm: hbList ) {
				cuPenmanMonteithMethodList.add ( pm.getMethod_desc());
			}
		}
		return cuPenmanMonteithMethodList;
	}
	else if ( prop.equalsIgnoreCase("DefaultWDIDLength") ) {
		return new Integer(__defaultWdidLength);
	}
    else if ( prop.equalsIgnoreCase("DebugLevelLogFile") ) {
        return new Integer(Message.getDebugLevel(Message.LOG_OUTPUT));
    }
    else if ( prop.equalsIgnoreCase("DebugLevelScreen") ) {
        return new Integer(Message.getDebugLevel(Message.TERM_OUTPUT));
    }
	else if ( prop.equalsIgnoreCase("HUCList") ) {
		// Get the list of HUC basin identifiers, for use in dialogs
		List hucList = __hdmi.getHUC();
		return hucList;
	}
	else if ( prop.equalsIgnoreCase("HydroBaseDMI") ) {
		return __hdmi;
	}
	else if ( prop.equalsIgnoreCase("HydroBaseDMIList") ) {
		List v = new Vector ();
		v.add ( __hdmi );
		return v;
	}
	else if ( prop.equalsIgnoreCase("HydroBase_SupplementalParcelUseTS_List") ||
		prop.equalsIgnoreCase("HydroBase_Supplemental_ParcelUseTS_List") ) { // TODO SAM 2009-02-11 remove
		return getHydroBaseSupplementalParcelUseTSList();
	}
	else if ( prop.equalsIgnoreCase("InitialWorkingDir") ) {
		return getInitialWorkingDir();
	}
	else if ( prop.equalsIgnoreCase("OutputComments") ) {
		return getPropContents_OutputComments();
	}
	else if ( prop.equalsIgnoreCase("OutputEnd") ) {
		return __OutputEnd_DateTime;
	}
	else if ( prop.equalsIgnoreCase("OutputStart") ) {
		return __OutputStart_DateTime;
	}
	else if ( prop.equalsIgnoreCase("OutputYearType") ) {
		return __OutputYearType;
	}
	else if ( prop.equalsIgnoreCase("NeedToCopyDiversionHistoricalTSMonthly") ) {
		return new Boolean(__need_diversion_ts_monthly_copy);
	}
	else if ( prop.equalsIgnoreCase("StateCU_BlaneyCriddle_List") ) {
		return getStateCUBlaneyCriddleList();
	}
	else if ( prop.equalsIgnoreCase("StateCU_CropCharacteristics_List") ) {
		return getStateCUCropCharacteristicsList();
	}
	else if ( prop.equalsIgnoreCase("StateCU_CropPatternTS_List") ) {
		return getStateCUCropPatternTSList();
	}
	else if ( prop.equalsIgnoreCase("StateCU_ClimateStation_List") ) {
		return getStateCUClimateStationList();
	}
	else if ( prop.equalsIgnoreCase("StateCU_DataSet") ) {
		return __StateCU_DataSet;
	}
	else if ( prop.equalsIgnoreCase("StateCU_IrrigationPracticeTS_List") ) {
		return getStateCUIrrigationPracticeTSList();
	}
	else if ( prop.equalsIgnoreCase("StateCU_Location_List") ) {
		return getStateCULocationList();
	}
	else if ( prop.equalsIgnoreCase("StateCU_PenmanMonteith_List") ) {
		return getStateCUPenmanMonteithList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_DataSet") ) {
		return __StateMod_DataSet;
	}
	else if ( prop.equalsIgnoreCase("StateMod_ConsumptiveWaterRequirementTSMonthly_List") ){
		return getStateModConsumptiveWaterRequirementTSMonthlyList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_DelayTableDaily_List") ){
		return getStateModDelayTableList(TimeInterval.DAY);
	}
	else if ( prop.equalsIgnoreCase("StateMod_DelayTableMonthly_List") ){
		return getStateModDelayTableList(TimeInterval.MONTH);
	}
	else if ( prop.equalsIgnoreCase("StateMod_DiversionDemandTSMonthly_List") ){
		return getStateModDiversionDemandTSMonthlyList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_DiversionHistoricalTSMonthly_List") ){
		return getStateModDiversionHistoricalTSMonthlyList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_DiversionHistoricalTSMonthlyCopy_List") ){
		return getStateModDiversionHistoricalTSMonthlyCopyList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_DiversionRight_List") ){
		return getStateModDiversionRightList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_DiversionStation_List")){
		return getStateModDiversionStationList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_InstreamFlowDemandTSAverageMonthly_List") ){
		return getStateModInstreamFlowDemandTSAverageMonthlyList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_InstreamFlowRight_List") ){
		return getStateModInstreamFlowRightList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_InstreamFlowStation_List")){
		return getStateModInstreamFlowStationList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_Network")){
		return getStateModNetwork();
	}
	else if ( prop.equalsIgnoreCase("StateMod_OperationalRight_List") ){
		return getStateModOperationalRightList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_PatternTSMonthly_List") ) {
		return getStateModPatternTSMonthlyList();
	}
	else if ( prop.equalsIgnoreCase("StateModPlanList") || prop.equalsIgnoreCase("StateMod_PlanStation_List") ) {
		return getStateModPlanStationList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_PlanReturn_List") ) {
		return getStateModPlanReturnList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_PlanWellAugmentation_List") ) {
		return getStateModPlanWellAugmentationList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_PrfGageData_List")){
		return getStateModPrfGageDataList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_ReservoirReturn_List") ) {
		return getStateModReservoirReturnList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_ReservoirRight_List") ){
		return getStateModReservoirRightList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_ReservoirStation_List")){
		return getStateModReservoirStationList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_RiverNetworkNode_List")){
		return getStateModRiverNetworkNodeList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_StreamEstimateCoefficients_List") ){
		return getStateModStreamEstimateCoefficientsList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_StreamEstimateStation_List") ){
		return getStateModStreamEstimateStationList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_StreamGageStation_List") ){
		return getStateModStreamGageStationList();
	}
	else if ( prop.equalsIgnoreCase("StateModWellList") || prop.equalsIgnoreCase("StateMod_Well_List") ||
		prop.equalsIgnoreCase("StateMod_WellStation_List") ) {
		return getStateModWellStationList();
	}
	else if ( prop.equalsIgnoreCase("StateModWellRightList") || prop.equalsIgnoreCase("StateMod_WellRight_List")) {
		return getStateModWellRightList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_WellHistoricalPumpingTSMonthly_List") ){
		return getStateModWellHistoricalPumpingTSMonthlyList();
	}
	else if ( prop.equalsIgnoreCase("StateMod_WellDemandTSMonthly_List") ){
		return getStateModWellDemandTSMonthlyList();
	}
    else if ( prop.equalsIgnoreCase("WarningLevelLogFile") ) {
        return new Integer(Message.getWarningLevel(Message.LOG_OUTPUT));
    }
    else if ( prop.equalsIgnoreCase("WarningLevelScreen") ) {
        return new Integer(Message.getWarningLevel(Message.TERM_OUTPUT));
    }
	else if ( prop.equalsIgnoreCase("WorkingDir") ) {
		return getWorkingDir();
	}
	else {
		throw new Exception ( "Requested property \"" + prop + "\" is not recognized by the StateDMI processor." );
	}
}

/**
Handle the OutputComments property request.  This includes, for example,
the commands that are active and HydroBase version information that documents
data available for a command.
@return Vector of String containing comments for output.
*/
private Vector getPropContents_OutputComments()
{
	Vector comments = new Vector();
	// Commands.  Show the file name but all commands may be in memory.
	comments.addElement ( "-----------------------------------------------------------------------" );
	String commands_filename = getCommandFileName();
	if ( commands_filename == null ) {
		comments.addElement ( "Command file name:  COMMANDS NOT SAVED TO FILE" );
	}
	else {
	    comments.addElement ( "Command file name: \"" + commands_filename + "\"" );
	}
	comments.addElement ( "Commands: " );
	List commandList = getCommands();
	int size_commands = commandList.size();
	for ( int i = 0; i < size_commands; i++ ) {
		comments.add ( ((Command)commandList.get(i)).toString() );
	}
	// Save information about data sources.
	HydroBaseDMI hbdmi = getHydroBaseDMIConnection();
	List hbdmi_Vector = new Vector();
	if ( hbdmi != null ) {
		hbdmi_Vector.add ( hbdmi );
	}
	int hsize = hbdmi_Vector.size();
	String db_comments[] = null;
	for ( int ih = 0; ih < hsize; ih++ ) {
		hbdmi = (HydroBaseDMI)hbdmi_Vector.get(ih);
		if ( hbdmi != null ) {
			try {
			    db_comments = hbdmi.getVersionComments ();
			}
			catch ( Exception e ) {
				db_comments = null;
			}
		}
		if ( db_comments != null ) {
			for ( int i = 0; i < db_comments.length; i++ ) {
				comments.addElement(db_comments[i]);
			}
		}
	}
	return new Vector(comments);
}

/**
Return the list of property names available from the processor.
These properties can be requested using getPropContents().
@return the list of property names available from the processor.
*/
public Collection getPropertyNameList()
{
	List v = new Vector();
	// FIXME SAM 2008-02-15 Evaluate whether these should be in the
	// property hashtable - should properties be available before ever
	// being defined (in case they are used later) or should only defined
	// properties be available (and rely on discovery to pass to other commands)?
	// Add properties that are hard-coded.
    v.add ( "DebugLevelLogFile" );
    v.add ( "DebugLevelScreen" );
    //v.add ( "HydroBaseDMI" );
 	v.add ( "OutputStart" );
	v.add ( "OutputEnd" );
    v.add ( "OutputYearType" );
    v.add ( "WarningLevelLogFile" );
    v.add ( "WarningLevelScreen" );
    v.add ( "WorkingDir" );
    // Create a set that includes the above.
    TreeSet set = new TreeSet(v);
    // Add the hashtable keys and make a unique list
    set.addAll ( __property_Hashtable.keySet() );
	return set;
}

/**
Determine if the commands are read-only.  In this case, applications may disable
save features.  The special comment "#@readOnly" indicates that the commands are read-only.
@return true if read-only, false if can be written.
*/
public boolean getReadOnly ()
{   // String that indicates readOnly
    String readOnlyString = "@readOnly";
    // Loop through the commands and check comments for the special string
    int size = size();
    Command c;
    List commandList = getCommands();
    for ( int i = 0; i < size; i++ ) {
        c = (Command)commandList.get(i);
        String commandString = c.toString();
        if ( commandString.trim().startsWith("#") &&
                (StringUtil.indexOfIgnoreCase(commandString,readOnlyString,0) > 0) ) {
            return true;
        }
    }
    return false;
}

/**
 * returns the status of the last run
 * false = errors encountered on run
 * true = successful run
 * @return runSuccessful
 */
public boolean getRunStatus()
{
	return runSuccessful;
}

/**
Return the list of StateCU_BlaneyCriddle being maintained by this StateDMI_Processor.
@return the list of StateCU_BlaneyCriddle being maintained by this StateDMI_Processor.
*/
public List getStateCUBlaneyCriddleList()
{	return __CUBlaneyCriddle_Vector;
}

/**
Return the list of StateCU_BlaneyCriddle matches being maintained by this StateDMI_Processor.
@return the list of StateCU_BlaneyCriddle matches being maintained by this StateDMI_Processor.
*/
public List getStateCUBlaneyCriddleMatchList()
{	return __CUBlaneyCriddle_match_Vector;
}

/**
Return the list of StateCU_ClimateStation being maintained by this StateDMI_Processor.
@return the list of StateCU_ClimateStation being maintained by this StateDMI_Processor.
*/
public List getStateCUClimateStationList()
{	return __CUClimateStation_Vector;
}

/**
Return the list of StateCU_ClimateStation matches being maintained by this StateDMI_Processor.
@return the list of StateCU_ClimateStation matches being maintained by this StateDMI_Processor.
*/
protected List getStateCUClimateStationMatchList()
{	return __CUClimateStation_match_Vector;
}

/**
Return the list of StateCU_CropCharacteristics being maintained by this StateDMI_Processor.
@return the list of StateCU_CropCharacteristics being maintained by this StateDMI_Processor.
*/
public List getStateCUCropCharacteristicsList()
{	return __CUCropCharacteristics_Vector;
}

/**
Return the list of StateCU_CropCharacteristics matches being maintained by this StateDMI_Processor.
@return the list of StateCU_CropCharacteristics matches being maintained by this StateDMI_Processor.
*/
protected List getStateCUCropCharacteristicsMatchList()
{	return __CUCropCharacteristics_match_Vector;
}


/**
Return the list of StateCU_CropPatternTS being maintained by this StateDMI_Processor.
@return the list of StateCU_CropPatternTS being maintained by this StateDMI_Processor.
*/
public List getStateCUCropPatternTSList()
{	return __CUCropPatternTS_Vector;
}

/**
Return the list of StateCU_CropPatternTS matches being maintained by this StateDMI_Processor.
@return the list of StateCU_CropPatternTS matches being maintained by this StateDMI_Processor.
*/
public List getStateCUCropPatternTSMatchList()
{	return __CUCropPatternTS_match_Vector;
}

/**
Return the list of StateCU_IrrigationPracticeTS being maintained by this StateDMI_Processor.
@return the list of StateCU_IrrigationPracticeTS being maintained by this StateDMI_Processor.
*/
public List getStateCUIrrigationPracticeTSList()
{	return __CUIrrigationPracticeTS_Vector;
}

/**
Return the list of StateCU_IrrigationPracticeTS matches being maintained by this StateDMI_Processor.
@return the list of StateCU_IrrigationPracticeTS matches being maintained by this StateDMI_Processor.
*/
public List getStateCUIrrigationPracticeTSMatchList()
{	return __CUIrrigationPracticeTS_match_Vector;
}

/**
Return the list of StateCU_Location being maintained by this StateDMI_Processor.
@return the list of StateCU_Location being maintained by this StateDMI_Processor.
*/
public List getStateCULocationList()
{	return __CULocation_Vector;
}

/**
Return the list of StateCU_Location matches being maintained by this StateDMI_Processor.
@return the list of StateCU_Location matches being maintained by this StateDMI_Processor.
*/
public List getStateCULocationMatchList()
{	return __CULocation_match_Vector;
}

/**
Return the list of StateCU_PenmanMonteith being maintained by this StateDMI_Processor.
@return the list of StateCU_PenmanMonteith being maintained by this StateDMI_Processor.
*/
public List<StateCU_PenmanMonteith> getStateCUPenmanMonteithList()
{	return __CUPenmanMonteith_Vector;
}

/**
Return the list of StateCU_BlaneyCriddle matches being maintained by this StateDMI_Processor.
@return the list of StateCU_BlaneyCriddle matches being maintained by this StateDMI_Processor.
*/
public List getStateCUPenmanMonteithMatchList()
{	return __CUPenmanMonteith_match_Vector;
}

/**
Return the output end date/time.
@return the output end date/time.
*/
public DateTime getOutputPeriodEnd()
{	return __OutputEnd_DateTime;
}

/**
Return the output start date/time.
@return the output start date/time.
*/
public DateTime getOutputPeriodStart()
{	return __OutputStart_DateTime;
}

/**
Return the list of consumptive water requirement monthly TS being maintained by this StateDMI_Processor.
@return the list of consumptive water requirement monthly TS being maintained by this StateDMI_Processor.
*/
public List getStateModConsumptiveWaterRequirementTSMonthlyList ()
{	return __SMConsumptiveWaterRequirementTSMonthlyList;
}

/**
Return the list of consumptive water requirement monthly TS matches being maintained by this StateDMI_Processor.
@return the list of consumptive water requirement monthly TS matches being maintained by this StateDMI_Processor.
*/
public List getStateModConsumptiveWaterRequirementTSMonthlyMatchList ()
{	return __SMConsumptiveWaterRequirementTSMonthly_match_Vector;
}

/**
Return the list of StateMod_DelayTable being maintained by this StateDMI_Processor.
These are used by both StateCU and StateMod data sets.
@param interval TimeInterval.MONTH or TimeInterval.DAY, indicating the interval for delay tables.
@return the list of StateMod_DelayTable being maintained by this StateDMI_Processor.
*/
public List getStateModDelayTableList ( int interval )
{	if ( interval == TimeInterval.MONTH ) {
		return __SMDelayTableMonthlyList;
	}
	else if ( interval == TimeInterval.DAY ) {
		return __SMDelayTableDailyList;
	}
	else {
		return null;
	}
}

/**
Return the list of StateMod_DelayTable matches being maintained by this StateDMI_Processor.
These are used by both StateCU and StateMod data sets.
@param interval TimeInterval.MONTH or TimeInterval.DAY, indicating the interval for delay tables.
@return the list of StateMod_DelayTable matches being maintained by this StateDMI_Processor.
*/
public List getStateModDelayTableMatchList ( int interval )
{	if ( interval == TimeInterval.MONTH ) {
		return __SMDelayTableMonthly_match_Vector;
	}
	else if ( interval == TimeInterval.DAY ) {
		return __SMDelayTableDaily_match_Vector;
	}
	else {
		return null;
	}
}

/**
Return the list of diversion demand daily TS being maintained by this StateDMI_Processor.
@return the list of diversion demand daily TS being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionDemandTSDailyList ()
{	return __SMDemandTSDailyList;
}

/**
Return the list of diversion demand monthly TS being maintained by this StateDMI_Processor.
@return the list of diversion demand monthly TS being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionDemandTSMonthlyList ()
{	return __SMDemandTSMonthlyList;
}

/**
Return the list of diversion demand monthly TS matches being maintained by this StateDMI_Processor.
@return the list of diversion demand monthly TS matches being maintained by this StateDMI_Processor.
*/
protected List getStateModDiversionDemandTSMonthlyMatchList ()
{	return __SMDemandTSMonthly_match_Vector;
}

/**
Return the list of diversion historical daily TS being maintained by this StateDMI_Processor.
@return the list of diversion historical daily TS being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionHistoricalTSDailyList ()
{	return __SMDiversionTSDailyList;
}

/**
Return the list of diversion historical monthly TS copy being maintained by this StateDMI_Processor.
@return the list of diversion historical monthly TS copy being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionHistoricalTSMonthlyCopyList ()
{	return __SMDiversionTSMonthly2List;
}

/**
Return the list of diversion historical monthly TS being maintained by this StateDMI_Processor.
@return the list of diversion historical monthly TS being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionHistoricalTSMonthlyList ()
{	return __SMDiversionTSMonthlyList;
}

/**
Return the list of diversion historical monthly TS matches being maintained by this StateDMI_Processor.
@return the list of diversion historical monthly TS matches being maintained by this StateDMI_Processor.
*/
protected List getStateModDiversionHistoricalTSMonthlyMatchList ()
{	return __SMDiversionTSMonthly_match_Vector;
}

/**
Return the list of StateMod_DiversionRight being maintained by this StateDMI_Processor.
@return the list of StateMod_DiversionRight being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionRightList ()
{	return __SMDiversionRightList;
}

/**
Return the list of StateMod_DiversionRight matches being maintained by this StateDMI_Processor.
@return the list of StateMod_DiversionRight matches being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionRightMatchList ()
{	return __SMDiversionRight_match_Vector;
}

/**
Return the list of StateMod_Diversion being maintained by this StateDMI_Processor.
@return the list of StateMod_Diversion being maintained by this StateDMI_Processor.
*/
public List<StateMod_Diversion> getStateModDiversionStationList ()
{	return __SMDiversionStationList;
}

/**
Return the list of StateMod_Diversion matches being maintained by this StateDMI_Processor.
@return the list of StateMod_Diversion matches being maintained by this StateDMI_Processor.
*/
public List getStateModDiversionStationMatchList ()
{	return __SMDiversion_match_Vector;
}

/**
Returns the list of instream flow demand (average monthly) time series
being maintained by the StateDMI processor.
@return the list of instream flow demand (average monthly) time series
being maintained by the StateDMI processor.
*/
public List getStateModInstreamFlowDemandTSAverageMonthlyList()
{	return __SMInstreamFlowDemandTSAverageMonthlyList;
}

/**
Returns the list of instream flow demand (average monthly) time series matches
being maintained by the StateDMI processor.
@return the list of instream flow demand (average monthly) time series matches
being maintained by the StateDMI processor.
*/
public List getStateModInstreamFlowDemandTSAverageMonthlyMatchList()
{	return __SMInstreamFlowDemandTSAverageMonthly_match_Vector;
}

/**
Returns the list of StateMod_InstreamFlowRight being maintained by the StateDMI processor.
@return the list of StateMod_InstreamFlowRight being maintained by the StateDMI processor.
*/
public List getStateModInstreamFlowRightList()
{	return __SMInstreamFlowRightList;
}

/**
Returns the list of StateMod_InstreamFlowRight matches being maintained by the StateDMI processor.
@return the list of StateMod_InstreamFlowRight matches being maintained by the StateDMI processor.
*/
public List getStateModInstreamFlowRightMatchList()
{	return __SMInstreamFlowRight_match_Vector;
}

/**
Returns the list of StateMod_InstreamFlow being maintained by the StateDMI processor.
@return the list of StateMod_InstreamFlow being maintained by the StateDMI processor.
*/
public List getStateModInstreamFlowStationList()
{	return __SMInstreamFlowStationList;
}

/**
Returns the list of StateMod_InstreamFlow matches being maintained by the StateDMI processor.
@return the list of StateMod_InstreamFlow matches being maintained by the StateDMI processor.
*/
public List getStateModInstreamFlowStationMatchList()
{	return __SMInstreamFlow_match_Vector;
}

/**
Returns the StateMod_Network being maintained by the StateDMI processor.
@return the StateMod_Network being maintained by the StateDMI processor.
*/
public StateMod_NodeNetwork getStateModNetwork()
{	return __SM_network;
}

/**
Return the list of StateMod_OperationalRight being maintained by this StateDMI_Processor.
@return the list of StateMod_OperationalRight being maintained by this StateDMI_Processor.
*/
public List<StateMod_OperationalRight> getStateModOperationalRightList ()
{	return __SMOperationalRightList;
}

/**
Return the list of StateMod_OperationalRight matches being maintained by this StateDMI_Processor.
@return the list of StateMod_OperationalRight matches being maintained by this StateDMI_Processor.
*/
public List<String> getStateModOperationalRightMatchList ()
{	return __SMOperationalRight_match_Vector;
}

/**
Return the list of pattern time series used with Fill*Pattern().
@return the list of pattern time series used with Fill*Pattern().
*/
protected List getStateModPatternTSMonthlyList()
{
	return __SMPatternTSMonthlyList;
}

/**
Return the list of StateMod_ReturnFlow for plan stations being maintained by this StateDMI_Processor.
@return the list of StateMod_ReturnFlow for plan stations being maintained by this StateDMI_Processor.
*/
public List<StateMod_ReturnFlow> getStateModPlanReturnList ()
{	return __SMPlanReturnList;
}

/**
Return the list of StateMod_Plan being maintained by this StateDMI_Processor.
@return the list of StateMod_Plan being maintained by this StateDMI_Processor.
*/
public List<StateMod_Plan> getStateModPlanStationList ()
{	return __SMPlanList;
}

/**
Return the list of StateMod_Plan_WellAugmentation being maintained by this StateDMI_Processor.
@return the list of StateMod_Plan_WellAugmentation being maintained by this StateDMI_Processor.
*/
public List<StateMod_Plan_WellAugmentation> getStateModPlanWellAugmentationList ()
{	return __SMPlanWellAugmentationList;
}

/**
Return the list of StateMod_Plan_Return matches being maintained by this StateDMI_Processor.
@return the list of StateMod_Plan_Return matches being maintained by this StateDMI_Processor.
*/
public List<String> getStateModPlanReturnMatchList ()
{	return __SMPlanReturn_match_Vector;
}

/**
Return the list of StateMod_Plan matches being maintained by this StateDMI_Processor.
@return the list of StateMod_Plan matches being maintained by this StateDMI_Processor.
*/
public List<String> getStateModPlanStationMatchList ()
{	return __SMPlan_match_Vector;
}

/**
Return the list of StateMod_Plan_WellAugmentation matches being maintained by this StateDMI_Processor.
@return the list of StateMod_Plan_WellAugmentation matches being maintained by this StateDMI_Processor.
*/
public List<String> getStateModPlanWellAugmentationMatchList ()
{	return __SMPlanWellAugmentation_match_Vector;
}

/**
Return the list of StateMod_PrfGageData being maintained by this StateDMI_Processor.
@return the list of StateMod_PrfGageData being maintained by this StateDMI_Processor.
*/
public List getStateModPrfGageDataList()
{	return __SMPrfGageData_Vector;
}

/**
Return the list of StateMod_ReturnFlow for reservoir stations being maintained by this StateDMI_Processor.
@return the list of StateMod_ReturnFlow for reservoir stations being maintained by this StateDMI_Processor.
*/
public List<StateMod_ReturnFlow> getStateModReservoirReturnList ()
{	return __SMReservoirReturnList;
}

/**
Return the list of StateMod_Reservoir_Return matches being maintained by this StateDMI_Processor.
@return the list of StateMod_Reservoir_Return matches being maintained by this StateDMI_Processor.
*/
public List<String> getStateModReservoirReturnMatchList ()
{	return __SMReservoirReturn_match_Vector;
}

/**
Return the list of StateMod_ReservoirRight being maintained by this StateDMI_Processor.
@return the list of StateMod_ReservoirRight being maintained by this StateDMI_Processor.
*/
public List getStateModReservoirRightList()
{	return __SMReservoirRightList;
}

/**
Return the list of StateMod_ReservoirRight matches being maintained by this StateDMI_Processor.
@return the list of StateMod_ReservoirRight matches being maintained by this StateDMI_Processor.
*/
public List getStateModReservoirRightMatchList()
{	return __SMReservoirRight_match_Vector;
}

/**
Return the list of StateMod_Reservoir being maintained by this StateDMI_Processor.
@return the list of StateMod_Reservoir being maintained by this StateDMI_Processor.
*/
public List getStateModReservoirStationList ()
{	return __SMReservoirStationList;
}

/**
Return the list of StateMod_Reservoir matches being maintained by this StateDMI_Processor.
@return the list of StateMod_Reservoir matches being maintained by this StateDMI_Processor.
*/
public List getStateModReservoirStationMatchList ()
{	return __SMReservoir_match_Vector;
}

/**
Return the list of StateMod_RiverNetworkNode being maintained by this StateDMI_Processor.
@return the list of StateMod_RiverNetworkNode being maintained by this StateDMI_Processor.
*/
public List getStateModRiverNetworkNodeList ()
{	return __SMRiverNetworkNode_Vector;
}

/**
Return the list of StateMod_RiverNetworkNode matches being maintained by this StateDMI_Processor.
@return the list of StateMod_RiverNetworkNode matches being maintained by this StateDMI_Processor.
*/
protected List getStateModRiverNetworkNodeMatchList ()
{	return __SMRiverNetworkNode_match_Vector;
}

/**
Return the list of StateMod_StreamEstimate being maintained by this StateDMI_Processor.
@return the list of StateMod_StreamEstimate being maintained by this StateDMI_Processor.
*/
public List getStateModStreamEstimateStationList()
{	return __SMStreamEstimateStationList;
}

/**
Return the list of StateMod_StreamEstimate matches being maintained by this StateDMI_Processor.
@return the list of StateMod_StreamEstimate matches being maintained by this StateDMI_Processor.
*/
public List getStateModStreamEstimateStationMatchList ()
{	return __SMStreamEstimate_match_Vector;
}

/**
Return the list of StateMod_StreamEstimateCoefficients being maintained by this StateDMI_Processor.
@return the list of StateMod_StreamEstimateCoefficients being maintained by this StateDMI_Processor.
*/
public List getStateModStreamEstimateCoefficientsList()
{	return __SMStreamEstimateCoefficients_Vector;
}

/**
Return the list of StateMod_StreamEstimate_Coefficients matches being maintained by this StateDMI_Processor.
@return the list of StateMod_StreamEstimate_Coefficients matches being maintained by this StateDMI_Processor.
*/
public List getStateModStreamEstimateCoefficientsMatchList ()
{	return __SMStreamEstimateCoefficients_match_Vector;
}

/**
Return the list of StateMod_StreamGage being maintained by this StateDMI_Processor.
@return the list of StateMod_StreamGage being maintained by this StateDMI_Processor.
*/
public List getStateModStreamGageStationList ()
{	return __SMStreamGageStationList;
}

/**
Return the list of StateMod_StreamGage matches being maintained by this StateDMI_Processor.
@return the list of StateMod_StreamGage matches being maintained by this StateDMI_Processor.
*/
public List getStateModStreamGageStationMatchList ()
{	return __SMStreamGage_match_Vector;
}

/**
Return the list of well demand monthly TS being maintained by this StateDMI_Processor.
@return the list of well demand monthly TS being maintained by this StateDMI_Processor.
*/
public List getStateModWellDemandTSMonthlyList ()
{	return __SMWellDemandTSMonthlyList;
}

/**
Return the list of well demand monthly TS matches being maintained by this StateDMI_Processor.
@return the list of well demand monthly TS matches being maintained by this StateDMI_Processor.
*/
protected List getStateModWellDemandTSMonthlyMatchList ()
{	return __SMWellDemandTSMonthly_match_Vector;
}

/**
Return the list of well historical pumping monthly TS being maintained by this StateDMI_Processor.
@return the list of well historical pumping monthly TS being maintained by this StateDMI_Processor.
*/
public List getStateModWellHistoricalPumpingTSMonthlyList ()
{	return __SMWellHistoricalPumpingTSMonthlyList;
}

/**
Return the list of well historical pumping monthly TS matches being maintained by this StateDMI_Processor.
@return the list of well historical pumping monthly TS matches being maintained by this StateDMI_Processor.
*/
public List getStateModWellHistoricalPumpingTSMonthlyMatchList ()
{	return __SMWellHistoricalPumpingTSMonthly_match_Vector;
}

/**
Return the list of StateMod_Well being maintained by this StateDMI_Processor.
@return the list of StateMod_Well being maintained by this StateDMI_Processor.
*/
public List<StateMod_Well> getStateModWellStationList ()
{	return __SMWellList;
}

/**
Return the list of StateMod_WellRight being maintained by this StateDMI_Processor.
@return the list of StateMod_WellRight being maintained by this StateDMI_Processor.
*/
public List getStateModWellRightList()
{	return __SMWellRightList;
}

/**
Return the list of StateMod_WellRight matches being maintained by this StateDMI_Processor.
@return the list of StateMod_WellRight matches being maintained by this StateDMI_Processor.
*/
protected List getStateModWellRightMatchList()
{	return __SMWellRight_match_Vector;
}

/**
Return the list of StateMod_Well matches being maintained by this StateDMI_Processor.
@return the list of StateMod_Well matches being maintained by this StateDMI_Processor.
*/
public List getStateModWellStationMatchList ()
{	return __SMWell_match_Vector;
}

/**
Return the TSSupplier name.
@return the TSSupplier name ("StateDMI_Processor").
*/
public String getTSSupplierName()
{	return "StateDMI_Processor";
}

/**
Find the position of a StateCU_Data object in the data Vector, using a WDID identifer.
The position for the first match is returned.  This method is included here
because putting in the StateCU package would require referencing HydroBaseDMI.
It is preferred to have StateCU stand on its own.
@return the position, or -1 if not found.
@param wd Water district.
@param id identifier.
*/
// TODO SAM 2007-02-18 Evaluate if needed
/*
private int indexOfCUDataUsingWDID ( Vector data, int wd, int id )
{	int size = 0;
	if ( data != null ) {
		size = data.size();
	}
	StateCU_Data d = null;
	int [] wdid_parts = new int[2];
	for (int i = 0; i < size; i++) {
		d = (StateCU_Data)data.get(i);
		try {	// Parse out the WDID and compare to that which is
			// passed in...
			HydroBase_WaterDistrict.parseWDID(d.getID(),wdid_parts);
			if ( (wdid_parts[0] == wd) && (wdid_parts[1] == id) ) {
				return i;
			}
		}
		catch ( Exception e ) {
			// Not a WDID...
			continue;
		}
	}
	return -1;
}
*/

/**
Return the current working directory for the processor.
@return the current working directory for the processor.
*/
protected String getWorkingDir ()
{	return __WorkingDir_String;
}

/**
Handle the CommandProcessorEvent events generated during processing and format for output.
Currently this method passes on the events to listeners registered on this processor.
@param event CommandProcessorEvent to handle.
*/
public void handleCommandProcessorEvent ( CommandProcessorEvent event )
{
    if ( __CommandProcessorEventListener_array != null ) {
        for ( int i = 0; i < __CommandProcessorEventListener_array.length; i++ ) {
            __CommandProcessorEventListener_array[i].handleCommandProcessorEvent(event);
        }
    }
}

/**
Determine the index of a command in the processor.  A reference comparison occurs.
@param command A command to search for in the processor.
@return the index (0+) of the matching command, or -1 if not found.
*/
public int indexOf ( Command command )
{	// Uncomment to troubleshoot
	//String routine = getClass().getName() + ".indexOf";
	int size = size();
	Command c;
	//Message.printStatus ( 2, routine, "Checking " + size + " commands for command " + command );
	for ( int i = 0; i < size; i++ ) {
		c = (Command)__commandList.get(i);
		//Message.printStatus ( 2, routine, "Comparing to command " + c );
		if ( c == command ) {
			//Message.printStatus ( 2, routine, "Found command." );
			return i;
		}
	}
	//Message.printStatus ( 2, routine, "Did not find command." );
	return -1;
}

/**
Add a command using the Command instance.
@param command Command to insert.
@param index Index (0+) at which to insert the command.
*/
public void insertCommandAt ( Command command, int index )
{	String routine = getClass().getName() + ".insertCommandAt";
	getCommands().add( index, command );
	// Also add this processor as a listener for events
    if ( command instanceof CommandProcessorEventProvider ) {
        CommandProcessorEventProvider ep = (CommandProcessorEventProvider)command;
        ep.addCommandProcessorEventListener(this);
    }
	notifyCommandListListenersOfAdd ( index, index );
	Message.printStatus(2, routine, "Inserted command object \"" + command + "\" at [" + index + "]" );
}

/**
Add a command using the string text.  This should currently only be
used for commands that do not have command classes, which perform
additional validation on the commands.  A GenericCommand instance will
be instantiated to maintain the string and allow command status to be set.
@param command_string Command string for command.
@param index Index (0+) at which to insert the command.
*/
public void insertCommandAt ( String command_string, int index )
{	String routine = getClass().getName() + ".insertCommandAt";
	Command command = new GenericCommand ();
	command.setCommandString ( command_string );
	insertCommandAt ( command, index );
	// Also add this processor as a listener for events
    if ( command instanceof CommandProcessorEventProvider ) {
        CommandProcessorEventProvider ep = (CommandProcessorEventProvider)command;
        ep.addCommandProcessorEventListener(this);
    }
	Message.printStatus(2, routine, "Creating generic command from string \"" + command_string + "\"." );
}

/**
Indicate whether the processing is running.
@return true if the command processor is running, false if not.
*/
public boolean getIsRunning ()
{	return __is_running;
}

/**
Search for a fill pattern TS.
@return reference to found StringMonthTS instance.
@param PatternID Fill pattern identifier to search for.
*/
public StringMonthTS lookupFillPatternTS ( String PatternID )
{	if ( PatternID == null ) {
		return null;
	}
	if ( __SMPatternTSMonthlyList == null ) {
		return null;
	}

	int nfill_pattern_ts = __SMPatternTSMonthlyList.size();

	StringMonthTS fill_pattern_ts_i = null;
	for ( int i = 0; i < nfill_pattern_ts; i++ ) {
		fill_pattern_ts_i =
			(StringMonthTS)__SMPatternTSMonthlyList.get(i);
		if ( fill_pattern_ts_i == null ) {
			continue;
		}
		if (	PatternID.equalsIgnoreCase(
			fill_pattern_ts_i.getLocation()) ) {
			return fill_pattern_ts_i;
		}
	}
	fill_pattern_ts_i = null;
	return null;
}

/**
Process the events from the MessageJDialog class.  If the "Cancel" button has
been pressed, then request that the time series processing should stop.
@param command If "Cancel", then a request will be made to cancel processing.
*/
public void messageJDialogAction ( String command )
{	if ( command.equalsIgnoreCase("Cancel") ) {
		setCancelProcessingRequested ( true );
	}
}

/**
Notify registered CommandListListeners about one or more commands being added.
@param index0 The index (0+) of the first command that is added.
@param index1 The index (0+) of the last command that is added.
*/
private void notifyCommandListListenersOfAdd ( int index0, int index1 )
{	if ( __CommandListListener_array != null ) {
		for ( int i = 0; i < __CommandListListener_array.length; i++ ) {
			__CommandListListener_array[i].commandAdded(index0, index1);
		}
	}
}

/**
Notify registered CommandListListeners about one or more commands being changed.
@param index0 The index (0+) of the first command that is added.
@param index1 The index (0+) of the last command that is added.
*/
private void notifyCommandListListenersOfChange ( int index0, int index1 )
{	if ( __CommandListListener_array != null ) {
		for ( int i = 0; i < __CommandListListener_array.length; i++ ) {
			__CommandListListener_array[i].commandChanged(index0, index1);
		}
	}
}

/**
Notify registered CommandListListeners about one or more commands being removed.
@param index0 The index (0+) of the first command that is added.
@param index1 The index (0+) of the last command that is added.
*/
private void notifyCommandListListenersOfRemove ( int index0, int index1 )
{	if ( __CommandListListener_array != null ) {
		for ( int i = 0; i < __CommandListListener_array.length; i++ ) {
			__CommandListListener_array[i].commandRemoved(index0, index1);
		}
	}
}

/**
Notify registered CommandProcessorListeners about a command being cancelled.
@param icommand The index (0+) of the command that is cancelled.
@param ncommand The number of commands being processed.  This will often be the
total number of commands but calling code may process a subset.
@param command The instance of the neareset command that is being cancelled.
*/
protected void notifyCommandProcessorListenersOfCommandCancelled (
		int icommand, int ncommand, Command command )
{	// This method is protected to allow TSEngine to call
	if ( __CommandProcessorListener_array != null ) {
		for ( int i = 0; i < __CommandProcessorListener_array.length; i++ ) {
			__CommandProcessorListener_array[i].commandCanceled(icommand,ncommand,command,-1.0F,
				"Command cancelled.");
		}
	}
}

/**
Notify registered CommandProcessorListeners about a command completing.
@param icommand The index (0+) of the command that is completing.
@param ncommand The number of commands being processed.  This will often be the
total number of commands but calling code may process a subset.
@param command The instance of the command that is completing.
*/
protected void notifyCommandProcessorListenersOfCommandCompleted (
		int icommand, int ncommand, Command command )
{	// This method is protected to allow TSEngine to call
	if ( __CommandProcessorListener_array != null ) {
		for ( int i = 0; i < __CommandProcessorListener_array.length; i++ ) {
			__CommandProcessorListener_array[i].commandCompleted(icommand,ncommand,command,-1.0F,"Command completed.");
		}
	}
}

/**
Notify registered CommandProcessorListeners about a command starting.
@param icommand The index (0+) of the command that is starting.
@param ncommand The number of commands being processed.  This will often be the
total number of commands but calling code may process a subset.
@param command The instance of the command that is starting.
*/
protected void notifyCommandProcessorListenersOfCommandStarted (
		int icommand, int ncommand, Command command )
{	// This method is protected to allow TSEngine to call
	if ( __CommandProcessorListener_array != null ) {
		for ( int i = 0; i < __CommandProcessorListener_array.length; i++ ) {
			__CommandProcessorListener_array[i].commandStarted(icommand,ncommand,command,-1.0F,"Command started.");
		}
	}
}

/**
Notify registered ProcessListener objects of a status change for processing
(e.g., cancel, error, success).
@param status numeric status code (see STATUS_*).
@param message String message to pass to the calling code.
*/
/*FIXME SAM 2007-10-18 Need to replace the following with the above
private void notifyListenersOfProcessStatus ( int status, String message )
{	if ( __listeners != null ) {
		for ( int i = 0; i <__listeners.length;i++){
			__listeners[i].processStatus ( status, message );
		}
	}
}
*/

/**
Process a list of commands, resulting in lists of data set objects and properties.  The resulting
objects can be displayed in the GUI.
<b>Filling with historical averages is handled for monthly time series
so that original data averages are used.</b>
@param commandList The Vector of Command from the this instance of StateDMI_Processor,
to be processed.  If null, process all.  Non-null is typically only used, for example,
if a user has selected commands in a GUI.
@param app_PropList if not null, then properties are set as the commands are
run.  This is typically used when running commands prior to using an edit
dialog in the StateDMI GUI.  Properties can have the following values:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>
</tr>

<tr>
<td><b>CreateOutput</b></td>
<td>Indicate whether output files should be created.  False is faster but
results in incomplete products.
</td>
<td>True - create output files.</td>
</tr>

<tr>
<td><b>InitialWorkingDir</b></td>
<td>The initial working directory for the run.  Normally this is only set when using a
command like RunCommands() to avoid confusion between the second command file and the
controlling command file.
</td>
<td>True - create output files.</td>
</tr>

</table>
*/
private void processCommands ( List<Command> commandList, PropList app_PropList )
throws Exception
{	String	message, routine = "StateDMI.processCommands";
	String message_tag = "ProcessCommands"; // Tag used with messages generated in this method.
	int error_count = 0;	// For errors during time series retrieval
	int update_count = 0;	// For warnings about command updates
	if ( commandList == null ) {
		// Process all commands if a subset has not been provided.
		commandList = getCommands();
	}
	
	// Save the passed in properties processRequest
	// call, so that they can be retrieved with other requests.
	
	if ( app_PropList == null ) {
		app_PropList = new PropList ( "StateDMI" );
	}

	/* FIXME SAM 2008-10-14 Evaluate if needed
	 * 	// Save class version...
	__processor_PropList = app_PropList;
	*/

	// Initialize the working directory to the initial directory that is
	// passed in.  Do this because software may request the working directory that
	// is the result of processing and the initial directory may never have
	// been changed dynamically.
	
	/* TODO SAM 2007-10-13 Remove when test out.  The initial working dir is no
	 * longer dynamic but is a data member on the processor.
	String InitialWorkingDir = __processor_PropList.getValue ( "InitialWorkingDir" );
	*/
	String InitialWorkingDir = getInitialWorkingDir(); //app_PropList.getValue("InitialWorkingDir");
	//if (InitialWorkingDir == null) {
	//	// None passed in so use what was previously defined for the processor (command file location).
	//	InitialWorkingDir = getInitialWorkingDir();
	//}
	// Set the working directory to the initial working directory
	if ( InitialWorkingDir != null ) {
		setPropContents ( "WorkingDir", InitialWorkingDir );
	}
	Message.printStatus(2, routine,"InitialWorkingDir=" + InitialWorkingDir );
	
	// Indicate whether output products/files should be created, or
	// just time series (to allow interactive graphing).
	String CreateOutput = app_PropList.getValue ( "CreateOutput" );
	if ( (CreateOutput != null) && CreateOutput.equalsIgnoreCase("False")){
		setCreateOutput ( false );
	}
	else {
		setCreateOutput ( true );
	}
	Message.printStatus(2, routine,"CreateOutput=" + getCreateOutput() );
	
	// Indicate whether time series should be cleared between runs.
	// If true, do not clear the time series between recursive
	// calls.  This is somewhat experimental to evaluate a master
	// commands file that runs other commands files.
	boolean AppendResults_boolean = false;

	int size = commandList.size();
	Message.printStatus ( 1, routine, "Processing " + size + " commands..." );
	StopWatch stopwatch = new StopWatch();
	stopwatch.start();
	String command_String = null;

	boolean inComment = false;
	Command command = null;	// The command to process
	CommandStatus command_status = null; // Put outside of main try to be able to use in catch.

	// Change setting to allow warning messages to be turned off during the main loop.
    // This capability should not be needed if a command uses the new command status processing.

	int popup_warning_level = 2;		// Do not popup warnings (only to log)
    // Turn off interactive warnings to pretent overload on user in loops.
    Message.setPropValue ( "ShowWarningDialog=false" );
    
    // Clear any settings that may have been left over from the previous run and which
    // can impact the current run.
    
    processCommands_ResetDataForRunStart ( AppendResults_boolean );

	// Now loop through the commands to process.

	inComment = false;
	int i_for_message;	// This will be adjusted by
				// __num_prepended_commands - the user will
				// see command numbers in messages like (12),
				// indicating the twelfth command.

	String command_tag = null;	// String used in messages to allow
					// link back to the application
					// commands, for use with each command.
	int i;	// Put here so can check count outside of end of loop
	boolean prev_command_complete_notified = false;// If previous command completion listeners were notified
										// May not occur if "continue" in loop.
	Command command_prev = null;	// previous command in loop
	CommandProfile commandProfile = null; // Profile to track execution time, memory use
	// Indicate the state of the processor...
	setIsRunning ( true );
	// Stopwatch to time each command...
	StopWatch stopWatch = new StopWatch();
	for ( i = 0; i < size; i++ ) {
		// 1-offset comand count for messages
		i_for_message = i + 1;
		command_tag = "" + i_for_message;	// Command number as integer 1+, for message/log handler.
		// If for some reason the previous command did not notify listeners of its completion (e.g., due to
		// continue in loop, do it now)...
		if ( !prev_command_complete_notified && (command_prev != null) ) {
			notifyCommandProcessorListenersOfCommandCompleted ( (i - 1), size, command_prev );
		}
		prev_command_complete_notified = false;
		// Save the previous command before resetting to new command below.
		if ( i > 0 ) {
			command_prev = command;
		}
		// Check for a cancel, which would have been set by pressing
		// the cancel button on the warning dialog or by using the other TSTool menus...
		if ( getCancelProcessingRequested() ) {
            // Turn on interactive warnings again.
            Message.setPropValue ( "ShowWarningDialog=true" );
			// Set flag so code interested in processor knows it is not running...
			setIsRunning ( false );
			// Reset the cancel processing request and let interested code know that
			// processing has been cancelled.
			setCancelProcessingRequested ( false );
			notifyCommandProcessorListenersOfCommandCancelled (	i, size, command );
			return;
		}
		try {
		    // Catch errors in all the commands.
    		command = commandList.get(i);
    		command_String = command.toString();
    		if ( command_String == null ) {
    			continue;
    		}
    		command_String = command_String.trim();
    		// All commands will implement CommandStatusProvider so get it...
    		command_status = ((CommandStatusProvider)command).getCommandStatus();
    		// Clear the run status (internally will set to UNKNOWN).
    		command_status.clearLog(CommandPhaseType.RUN);
    		commandProfile = command.getCommandProfile(CommandPhaseType.RUN);
    		Message.printStatus ( 1, routine, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    		Message.printStatus ( 1, routine,
    			"Start processing command " + (i + 1) + " of " + size + ": \"" + command_String + "\" " );
    		stopWatch.clear();
    		stopWatch.start();
    		commandProfile.setStartTime(System.currentTimeMillis());
            commandProfile.setStartHeap(Runtime.getRuntime().totalMemory());
    		// Notify any listeners that the command is running...
    		notifyCommandProcessorListenersOfCommandStarted ( i, size, command );
    
    		if ( command instanceof Comment_Command ) {
    			// Comment.  Mark as processing successful.
    			command_status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
    			command_status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
    			commandProfile.setEndTime(System.currentTimeMillis());
                commandProfile.setEndHeap(Runtime.getRuntime().totalMemory());
    			continue;
    		}
    		else if ( command instanceof CommentBlockStart_Command ) {
    			inComment = true;
    			command_status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
    			command_status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
    			commandProfile.setEndTime(System.currentTimeMillis());
                commandProfile.setEndHeap(Runtime.getRuntime().totalMemory());
    			continue;
    		}
    		else if ( command instanceof CommentBlockEnd_Command ) {
    			inComment = false;
    			command_status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
    			command_status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
    			commandProfile.setEndTime(System.currentTimeMillis());
                commandProfile.setEndHeap(Runtime.getRuntime().totalMemory());
    			continue;
    		}
    		if ( inComment ) {
    		    // Commands won't know themselves that they are in a comment so set the status for them
    		    // and continue.
    		    // TODO SAM 2008-09-30 Do the logs need to be cleared?
    			command_status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
    			command_status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
    			commandProfile.setEndTime(System.currentTimeMillis());
                commandProfile.setEndHeap(Runtime.getRuntime().totalMemory());
    			continue;
    		}
    		else if ( command instanceof Exit_Command ) {
    			// Exit the processing...
    			Message.printStatus ( 1, routine, "Exit - stop processing commands." );
    			commandProfile.setEndTime(System.currentTimeMillis());
                commandProfile.setEndHeap(Runtime.getRuntime().totalMemory());
    			break;
    		}
    	
    		// Check for obsolete commands (do this last to minimize the amount of processing through this code)...
    		// Do this at the end because this logic may seldom be hit if valid commands are processed above.  
    		/* FIXME SAM 2008-10-14 Evaluate whether needed
    		else if ( processCommands_CheckForObsoleteCommands(command_String, (CommandStatusProvider)command, message_tag, i_for_message) ) {
    			// Had a match so increment the counters.
    			++update_count;
    			++error_count;
    		}
    		*/
    		// Command factory for remaining commands...
    		else {
                // Try the Command class code...
    			try {
                    // Make sure the command is valid...
    				// Initialize the command (parse)...
    				// TODO SAM 2007-09-05 Need to evaluate where the initialization occurs (probably the initial edit or load)?
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( 1, routine, "Initializing the Command for \"" +	command_String + "\"" );
    				}
    				if ( command instanceof CommandStatusProvider ) {
    					((CommandStatusProvider)command).getCommandStatus().clearLog(CommandPhaseType.INITIALIZATION);
    				}
    				if ( command instanceof CommandStatusProvider ) {
    					((CommandStatusProvider)command).getCommandStatus().clearLog(CommandPhaseType.DISCOVERY);
    				}
    				command.initializeCommand ( command_String, this, true );
    				// TODO SAM 2005-05-11 Is this the best place for this or should it be in RunCommand()?
    				// Check the command parameters...
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( 1, routine, "Checking the parameters for command \"" + command_String + "\"" );
    				}
    				command.checkCommandParameters ( command.getCommandParameters(), command_tag, 2 );
    				// Clear the run status for the command...
    				if ( command instanceof CommandStatusProvider ) {
    					((CommandStatusProvider)command).getCommandStatus().clearLog(CommandPhaseType.RUN);
    				}
    				// Run the command...
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( 1, routine, "Running command through new code..." );
    				}
    				command.runCommand ( i_for_message );
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( 1, routine, "...back from running command." );
    				}
    			}
    			catch ( InvalidCommandSyntaxException e ) {
    				message = "Unable to process command - invalid syntax (" + e + ").";
    				if ( command instanceof CommandStatusProvider ) {
    				       if (	CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).
                                   greaterThan(CommandStatusType.UNKNOWN) ) {
    				           // No need to print a message to the screen because a visual marker will be shown, but log...
    				           Message.printWarning ( 2,
    				                   MessageUtil.formatMessageTag(command_tag,
    				                           ++error_count), routine, message );
                           }
    				}
    				else {
    				    // Command has not been updated to set warning/failure in status so show here
    					Message.printWarning ( popup_warning_level,
    						MessageUtil.formatMessageTag(command_tag,
    						++error_count), routine, message );
    				}
    				// Log the exception.
    				if (Message.isDebugOn) {
    					Message.printDebug(3, routine, e);
    				}
    				continue;
    			}
    			catch ( InvalidCommandParameterException e ) {
    				message = "Unable to process command - invalid parameter (" + e + ").";
    				if ( command instanceof CommandStatusProvider ) {
    				    if ( CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).
                                greaterThan(CommandStatusType.UNKNOWN) ) {
    				        // No need to print a message to the screen because a visual marker will be shown, but log...
    				        Message.printWarning ( 2,
    				                MessageUtil.formatMessageTag(command_tag,
    				                        ++error_count), routine, message );
                        }
    				}
    				else {
    				    // Command has not been updated to set warning/failure in status so show here
    					Message.printWarning ( popup_warning_level,
    							MessageUtil.formatMessageTag(command_tag,
    									++error_count), routine, message );
    				}
    				if (Message.isDebugOn) {
    					Message.printWarning(3, routine, e);
    				}
    				continue;
    			}
    			catch ( CommandWarningException e ) {
    				message = "Warnings were generated processing command - output may be incomplete (" + e + ").";
    				if ( command instanceof CommandStatusProvider ) {
    				    if ( CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).
                                greaterThan(CommandStatusType.UNKNOWN) ) {
    				        // No need to print a message to the screen because a visual marker will be shown, but log...
    				        Message.printWarning ( 2,
    				                MessageUtil.formatMessageTag(command_tag,
    				                        ++error_count), routine, message );
                        }
    				}
    				else {	// Command has not been updated to set warning/failure in status so show here
    					Message.printWarning ( popup_warning_level,
    							MessageUtil.formatMessageTag(command_tag,
    									++error_count), routine, message );
    				}
    				if (Message.isDebugOn) {
    					Message.printDebug(3, routine, e);
    				}
    				continue;
    			}
    			catch ( CommandException e ) {
    				message = "Error processing command - unable to complete command (" + e + ").";
    				if ( command instanceof CommandStatusProvider ) {
    				    if ( CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).
                                greaterThan(CommandStatusType.UNKNOWN) ) {
    				        // No need to print a message to the screen because a visual marker will be shown, but log...
    				        Message.printWarning ( 2,
    				                MessageUtil.formatMessageTag(command_tag,++error_count), routine, message );
                        }
    				}
    				else {
    				    // Command has not been updated to set warning/failure in status so show here
    					Message.printWarning ( popup_warning_level,
    						MessageUtil.formatMessageTag(command_tag,
    						++error_count), routine, message );
    				}
    				if (Message.isDebugOn) {
    					Message.printDebug(3, routine, e);
    				}
    				continue;
    			}
    			catch ( Exception e ) {
    				message = "Unexpected error processing command - unable to complete command (" + e + ").";
    				if ( command instanceof CommandStatusProvider ) {
    					// Add to the log as a failure...
    					Message.printWarning ( 2,
    						MessageUtil.formatMessageTag(command_tag,++error_count), routine, message );
                        // Always add to the log because this type of exception is unexpected from a Command object.
    					command_status.addToLog(CommandPhaseType.RUN,
    							new CommandLogRecord(CommandStatusType.FAILURE,
    									"Unexpected exception \"" + e.getMessage() + "\"",
    									"See log file for details.") );
    				}
    				else {
    					Message.printWarning ( popup_warning_level,
    							MessageUtil.formatMessageTag(command_tag,
    									++error_count), routine, message );
    				}
    				Message.printWarning ( 3, routine, e );
    				continue;
    			}
    			finally {
    				// Save the time spent running the command
    	    		stopWatch.stop();
    	    		commandProfile.setEndTime(System.currentTimeMillis());
                    commandProfile.setEndHeap(Runtime.getRuntime().totalMemory());
    			}
    		}
		} // Main catch
		catch ( Exception e ) {
			Message.printWarning ( popup_warning_level, MessageUtil.formatMessageTag(command_tag,
			++error_count), routine, "There was an error processing command: \"" + command_String +
			"\".  Cannot continue processing." );
			Message.printWarning ( 3, routine, e );
			if ( command instanceof CommandStatusProvider ) {
				// Add to the command log as a failure...
				command_status.addToLog(CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
								"Unexpected error \"" + e.getMessage() + "\"", "See log file for details.") );
			}
		}
		catch ( OutOfMemoryError e ) {
		    message = "The command processor ran out of memory. (" + e + ").";
			Message.printWarning ( popup_warning_level,
			MessageUtil.formatMessageTag(command_tag,
			++error_count), routine, message );
			if ( command instanceof CommandStatusProvider ) {
				// Add to the command log as a failure...
				command_status.addToLog(CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE, message,
						"Try increasing JRE memory with -Xmx and restarting the software.  " +
						"See the log file for details.  See troubleshooting documentation.") );
			}
			Message.printWarning ( 2, routine, e );
			System.gc();
			// May be able to save commands.
		}
		finally {
			// Always want to get to here for each command.
		}
		// Notify any listeners that the command is done running...
		prev_command_complete_notified = true;
		notifyCommandProcessorListenersOfCommandCompleted ( i, size, command );
		Message.printStatus ( 1, routine,
            "Done processing command \"" + command_String + "\" (" +  (i + 1) + " of " + size + " commands, " +
            StringUtil.formatString(commandProfile.getRunTime(),"%d") + " ms runtime)" );
        Message.printStatus ( 1, routine, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
	}
	// If necessary, do a final notify for the last command...
	if ( !prev_command_complete_notified ) {
		if ( i == size ) {
			--i;
		}
		notifyCommandProcessorListenersOfCommandCompleted ( i, size, command );
	}
	
	// Indicate that processing is done and now there is no need to worry about cancelling.
	setIsRunning ( false );
	if ( getCancelProcessingRequested() ) {
		// Have gotten to here probably because the last command was processed
		// and need to notify the listeners.
		notifyCommandProcessorListenersOfCommandCancelled (	i, size, command );
	}
	setCancelProcessingRequested ( false );
	
    // Make sure that important warnings are shown to the user...
    Message.setPropValue ( "ShowWarningDialog=true" );

	// Get the final time - note this includes intervening warnings if any occurred...

	stopwatch.stop();
	Message.printStatus ( 1, routine, "Processing took " +
		StringUtil.formatString(stopwatch.getSeconds(),"%.2f") + " seconds" );

	// Check for fatal errors (for Command classes, only warn if failures since
	// others are likely not a problem)...

	int ml = 2;	// Message level for cleanup warnings
	CommandStatusType max_severity = CommandStatusProviderUtil.getHighestSeverity ( commandList );
	if ( (error_count > 0) || max_severity.greaterThan(CommandStatusType.WARNING)) {

		if ( IOUtil.isBatch() ) {
			// The following should will be passed through StateDMI_Processor.RunCommands() and should
			// be caught when using StateDMI_Processor_ThreadRunner.runCommands().
			message = "There were warnings or failures processing commands.  The output may be incomplete.";
			Message.printWarning ( ml, routine, message );
			throw new RuntimeException ( message );
		}
		else {
		    Message.printWarning ( ml, routine,
			"There were warnings processing commands.  The output may be incomplete.\n" +
			"See the log file for information." );
		}
	}
	if ( update_count > 0 ) {
		Message.printWarning ( ml, routine,
		"There were warnings printed for obsolete commands.\n" +
		"See the log file for information.  The output may be incomplete." );
	}
}

/**
Process a set of StateDMI commands, resulting in data objects being created and
managed in memory.  The database connections, commands, etc. should be passed
in at creation or with set() methods.  This method is called from the run()
method for use with threaded execution.
@param command_Vector The Vector of Command to process.  If null, process all.
Non-null is typically only used, for example,
if a user has selected commands in a GUI.
@param app_PropList if not null, then properties are set as the commands are
run.  This is typically used when running commands prior to using an edit
dialog in the TSTool GUI, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>
</tr>

<tr>
<td><b>CreateOutput</b></td>
<td>Indicate whether output files should be created.  False is faster but
results in incomplete products.
</td>
<td>True - create output files.</td>
</tr>
</table>
@exception Exception If there is an error in the commands file.
*/
private void processCommands_OLD ( List command_Vector, PropList props )
throws Exception
{	String	message, routine = "StateDMI_Processor.processCommands";
	// FIXME SAM 2007-10-22 Need to convert to a property
	// if false, then run is during command initialization (e.g., to get the WorkingDir
	// for the command being edited).  Maybe this will not be needed.
	boolean full_run = true;
	// Clean out errors...
	__command_warning_Vector.clear();
	int warning_count = 0;	// For warnings printed in this method.
	int update_count = 0;	// For warnings about commands that are
				// obsolete or need updating
	runSuccessful = true;

	List commands = command_Vector;	// Commands to process
	if ( command_Vector == null ) {
		// Process all commands
		commands = command_Vector;
	}
	int size = commands.size();

	if ( full_run ) {
		Message.printStatus (2, routine,
			"Processing " + size + " commands...");
	}
	Command command = null;	// Command being processed
	String command_String = null;	// String representation of command

	// Go through the commands up front one time and set some important
	// flags to help performance, etc....

	// Now loop through the commands, process data, and manipulate objects
	// in memory to produce final data objects.  The following loop does the
	// initial queries.

	boolean in_comment = false;
				// Indicates if in a multi-line comment.
	int i_for_message;	// This will be adjusted by
				// __num_prepended_commands - the user will
				// see command numbers in messages like (12),
				// indicating the twelfth command.
	int i_for_notify;	// Zero index position for commands, adjusted
				// for __num_prepended_commands
	
	// Reset all the data/results vectors to be empty.  Currently each time the
	// commands are processed, the vectors are cleared.  Later might find
	// a way to control this more precisely, especially if the tree is used
	// in the main GUI...
	
	processCommands_ResetDataForRunStart ( false );

	// Allow the log file to be seen when errors occur...

	if ( full_run ) {
		// Only do this for a full run.  Otherwise, editing commands
		// while a run is occurring may reset the values and the user
		// will not be able to cancel the full run or look at the log
		// file...
		Message.setPropValue ( "WarningDialogOKNoMoreButton=true" );
		Message.setPropValue ( "WarningDialogViewLogButton=true");
		Message.setPropValue ( "WarningDialogCancelButton=true");
		// The following will be set to true if a cancel occurs during
		// processing.
		MessageJDialog.addMessageJDialogListener ( this );
	}

	// This is a little ugly but do it for now...
	// Determine whether a limitDiversionHistoricalTSMonthlyToRights()
	// command is being used.  If so, then save a copy of the time series
	// after reading from HydroBase.  This will be used to ignore the
	// rights-imposed values.

	__need_diversion_ts_monthly_copy = false;
	String command_tag = null;	// String used in messages to allow
					// link back to the application
					// commands, for use with each command.
	String message_tag = "ProcessCommands";
					// Tag used with messages generated in
					// this method.

	for ( int i = 0; i < size; i++ ) {
		command = (Command)commands.get(i);
		command_String = command.toString().trim();
		if (	command_String.regionMatches(true, 0,
			"limitDiversionHistoricalTSMonthlyToRights", 0, 41)) {
			__need_diversion_ts_monthly_copy = true;
			Message.printStatus ( 2, routine,
			"Will make copy of diversion historical time series " +
			"after HydroBase read or set for use by " +
			"limitDiversionHistoricalTSMonthlyToRights()" );
			break;
		}
	}

	int i;	// Put here so can check count outside of end of loop
	boolean prev_command_complete_notified = false;// If previous command completion listeners were notified
										// May not occur if "continue" in loop.
	Command command_prev = null;	// previous command in loop
	for ( i = 0; i < size; i++ ) {

		// For example, setWorkingDir() is often prepended automatically
		// to start in the working directory.  In this case,
		// the first user-defined command will have:
		// i_for_message = 1 - 1 + 1 = 1
		i_for_message = i - __num_prepended_commands + 1;
		i_for_notify = i_for_message - 1;// Expected to be
						// zero-referenced
		command_tag = ""+i_for_message;	// Command number as integer 1+,
						// for message/log handler.
		try {	// Catch errors in all the commands.
			// Do not indent the body inside the try!
		// Notify listeners that the previous command finished (whether
		// successful or not)...
		/* FIXME SAM 2007-10-12 Remove when new code tests out
		if ( i > 0 ) {
			notifyListenersOfProcessStatus (
				STATUS_COMMAND_COMPLETE, "" + i_for_notify );
		}
		*/
		// If for some reason the previous command did not notify listeners of its completion (e.g., due to
		// continue in loop, do it now)...
		if ( !prev_command_complete_notified && (command_prev != null) ) {
			notifyCommandProcessorListenersOfCommandCompleted ( (i - 1), size, command_prev );
		}
		prev_command_complete_notified = false;
		// Save the previous command before resetting to new command below.
		if ( i > 0 ) {
			command_prev = command;
		}
		// Check for a cancel, which would have been set by pressing
		// the cancel button on the warning dialog or by using the other menus...
		if ( getCancelProcessingRequested() ) {
			// Set Warning dialog settings back to normal...
			if ( full_run ) {
				Message.setPropValue ( "WarningDialogViewLogButton=false" );
				Message.setPropValue ( "WarningDialogOKNoMoreButton=false" );
				Message.setPropValue ( "WarningDialogCancelButton=false" );
				Message.setPropValue ( "ShowWarningDialog=true" );
			}
			// Set flag so code interested in processor knows it is not running...
			setIsRunning ( false );
			// Reset the cancel processing request and let interested code know that
			// processing has been cancelled.
			setCancelProcessingRequested ( false );
			notifyCommandProcessorListenersOfCommandCancelled (	i, size, command );
			return;
		}
		// Should never have null commands...
		command = (Command)commands.get(i);
		command_String = command.toString().trim();

		Message.printStatus ( 1, routine, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		Message.printStatus ( 1, routine,
			"Start processing command " + (i + 1) + " of " + size + ": \"" + command_String + "\"" );
		
		// Notify any listeners that the command is running...
		notifyCommandProcessorListenersOfCommandStarted ( i, size, command );
		/* FIXME SAM remove old code when above tests out.
		if ( !in_comment ) {
			// Only notify when processing actual commands...
			notifyListenersOfProcessStatus (
				STATUS_COMMAND_START_NUM,"" + i_for_notify );
			notifyListenersOfProcessStatus (
				STATUS_COMMAND_START, command);
		}
		else {
			notifyListenersOfProcessStatus (
				STATUS_COMMAND_START_NUM,""+i_for_notify);
			notifyListenersOfProcessStatus (
				STATUS_COMMAND_START, command+" (commented)" );
		}
		*/

		// Check comments at the top because commands can be skipped...

		if ( command_String.equals("") ) {
			// Empty line...
			continue;
		}
		if (command_String.startsWith("#")) {
			// Comment...
			continue;
		}
		else if (command_String.startsWith("/*")) {
			in_comment = true;
			continue;
		}
		else if (command_String.endsWith("*/")) {
			in_comment = false;
			continue;
		}
		if ( in_comment ) {
			continue;
		}

		// Read commands...

		else if (command_String.regionMatches(true, 0,
			"readAgStatsTSFromDateValue", 0, 26)) {
			do_readAgStatsTSFromDateValue(command_tag,command_String);
		}

		/* FIXME SAM 2009-02-11 Enable when ready
		else if (command_String.regionMatches(true, 0,
			"readCropPatternTSFromDBF", 0, 24)) {
			do_readCropParcels(command_tag, command_String, true);
		}
		*/

		else if ( processCommands_CheckForObsoleteCommands(command_String, message_tag, i_for_message) ) {
			// Had a match so increment the counters.
			++update_count;
			++warning_count;
		}
		else {	// Try the Command class code...
				try {	// Make sure the command is valid...
					// Initialize the command (parse)...
					// TODO SAM 2007-09-05 Need to evaluate where the
					// initialization occurs (probably the initial edit or load)?
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine,
						"Initializing the Command for \"" +
						command_String + "\"" );
					}
					if ( command instanceof CommandStatusProvider ) {
						((CommandStatusProvider)command).getCommandStatus().clearLog(CommandPhaseType.INITIALIZATION);
					}
					if ( command instanceof CommandStatusProvider ) {
						((CommandStatusProvider)command).getCommandStatus().clearLog(CommandPhaseType.DISCOVERY);
					}
					command.initializeCommand ( command_String, this, true );
					// REVISIT SAM 2005-05-11 Is this the best
					// place for this or should it be in the
					// runCommand()?...
					// Check the command parameters...
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine,
						"Checking the parameters for command \""
						+ command_String + "\"" );
					}
					command.checkCommandParameters (
						command.getCommandParameters(),
						command_tag, 2 );
					// Clear the run status for the command...
					if ( command instanceof CommandStatusProvider ) {
						((CommandStatusProvider)command).getCommandStatus().clearLog(CommandPhaseType.RUN);
					}
					// Run the command...
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine,
						"Running command through new code..." );
					}
					command.runCommand ( i_for_message );
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine,
						"...back from running command." );
					}
				}
				catch ( InvalidCommandSyntaxException e ) {
					message = "Unable to process command (invalid syntax).";
					if ( command instanceof CommandStatusProvider &&
							CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).greaterThan(CommandStatusType.UNKNOWN) ) {
						// No need to print a message to the screen because a visual marker will be shown, but log...
						Message.printWarning ( 2,
							MessageUtil.formatMessageTag(command_tag,
							++warning_count), routine, message );
					}
					else {	// Command has not been updated to set warning/failure in status so show here
						Message.printWarning ( 1,
							MessageUtil.formatMessageTag(command_tag,
							++warning_count), routine, message );
					}
					// Log the exception.
					if (Message.isDebugOn) {
						Message.printDebug(3, routine, e);
					}
					continue;
				}
				catch ( InvalidCommandParameterException e ) {
					message = "Unable to process command invalid parameter).";
					if ( command instanceof CommandStatusProvider &&
							CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).greaterThan(CommandStatusType.UNKNOWN) ) {
						// No need to print a message to the screen because a visual marker will be shown, but log...
						Message.printWarning ( 2,
							MessageUtil.formatMessageTag(command_tag,
							++warning_count), routine, message );
					}
					else {	// Command has not been updated to set warning/failure in status so show here
						Message.printWarning ( 1,
								MessageUtil.formatMessageTag(command_tag,
										++warning_count), routine, message );
					}
					if (Message.isDebugOn) {
						Message.printWarning(3, routine, e);
					}
					continue;
				}
				catch ( CommandWarningException e ) {
					message = "Warnings were generated processing "+
						"command (output may be incomplete).";
					if ( command instanceof CommandStatusProvider &&
							CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).greaterThan(CommandStatusType.UNKNOWN) ) {
						// No need to print a message to the screen because a visual marker will be shown, but log...
						Message.printWarning ( 2,
							MessageUtil.formatMessageTag(command_tag,
							++warning_count), routine, message );
					}
					else {	// Command has not been updated to set warning/failure in status so show here
						Message.printWarning ( 1,
								MessageUtil.formatMessageTag(command_tag,
										++warning_count), routine, message );
					}
					if (Message.isDebugOn) {
						Message.printDebug(3, routine, e);
					}
					continue;
				}
				catch ( CommandException e ) {
					message = "Error processing command (unable to complete command).";
					if ( command instanceof CommandStatusProvider &&
							CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).greaterThan(CommandStatusType.UNKNOWN) ) {
						// No need to print a message to the screen because a visual marker will be shown, but log...
						Message.printWarning ( 2,
							MessageUtil.formatMessageTag(command_tag,
							++warning_count), routine, message );
					}
					else {	// Command has not been updated to set warning/failure in status so show here
						Message.printWarning ( 1,
							MessageUtil.formatMessageTag(command_tag,
							++warning_count), routine, message );
					}
					if (Message.isDebugOn) {
						Message.printDebug(3, routine, e);
					}
					continue;
				}
				catch ( Exception e ) {
					message = "Unexpected error processing command (unable to complete command).";
					if ( command instanceof CommandStatusProvider ) {
						// Add to the log as a failure...
						Message.printWarning ( 2,
							MessageUtil.formatMessageTag(command_tag,
							++warning_count), routine, message );
						((CommandStatusProvider)command).getCommandStatus().addToLog(CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
										"Unexpected exception \"" + e.getMessage() + "\"",
										"See log file for details.") );
					}
					else {
						Message.printWarning ( 1,
								MessageUtil.formatMessageTag(command_tag,
										++warning_count), routine, message );
					}
					Message.printWarning ( 3, routine, e );
					continue;
				}
		}

		} // Main catch
		catch (Exception e) {
			if ( Message.isDebugOn ) {
				Message.printDebug (1, routine, e);
			}
			// TODO SAM 2004-04-05 - comment this next line for
			// when software is finalized - then debug messages can
			// be used.
			Message.printWarning (__FYI_warning_level, routine, e);
			Message.printWarning (1,
			formatMessageTag(message_tag,i_for_message), routine,
			"There was an error processing command (" +
			(i_for_message) + "): " + command);
			if ( Message.isDebugOn ) {
				Message.printDebug (1, routine,
				"See above exception");
			}
			// Keep track of the commands with errors (zero index
			// command number without prepended commands)...
			__command_warning_Vector.add( "" +
				(i_for_message - 1) );
		}
		catch (OutOfMemoryError e) {
			Message.printWarning (1,
			formatMessageTag(message_tag,i_for_message), routine,
			"StateDMI ran out of memory.  Exit and restart.\n" +
			"Try increasing the -mx setting in StateDMI.bat.");
			System.gc();
			// May be able to save commands.
		}
		finally {
			// Just want to make sure that we get to this point.
		}
		// Notify any listeners that the command is done running...
		prev_command_complete_notified = true;
		notifyCommandProcessorListenersOfCommandCompleted ( i, size, command );
	}
	
	// Indicate that processing is done and now there is no need to worry about
	// cancelling.
	setIsRunning ( false );
	if ( getCancelProcessingRequested() ) {
		// Have gotten to here probably because the last command was processed
		// and need to notify the listeners.
		notifyCommandProcessorListenersOfCommandCancelled (	i, size, command );
	}
	setCancelProcessingRequested ( false );
	
	// If necessary, do a final notify for the last command...
	if ( !prev_command_complete_notified ) {
		if ( i == size ) {
			--i;
		}
		notifyCommandProcessorListenersOfCommandCompleted ( i, size, command );
	}

	// Change so from this point user always has to acknowledge the
	// warnings...

	if ( full_run ) {
		Message.setPropValue ( "WarningDialogOKNoMoreButton=false" );
		Message.setPropValue ( "WarningDialogCancelButton=false" );
		Message.setPropValue ( "ShowWarningDialog=true" );
		Message.setPropValue ( "WarningDialogViewLogButton=false" );
	}

	// Notify listeners that the last command finished (whether
	// successful or not)...
	/* FIXME remove when other code complete
	notifyListenersOfProcessStatus ( STATUS_COMMAND_COMPLETE, ""
		+ (size - __num_prepended_commands - 1) );
	*/

	// Check whether there are any issues with the consistency of the data
	// set (only if doing a full run)...

	if ( full_run ) {
	/* TODO SAM 2006-04-11
	Try new approach where checks are done on products.
	int check_warning_count = checkDataSet ();
	if ( check_warning_count > 0 ) {
		Message.printWarning ( 1, routine,
		"Checking the data set resulted in " + check_warning_count +
		" warnings." );
	}
	*/

	/**
	Vector check_Vector = __StateMod_DataSet.getDataCheckResults();
	int check_Vector_size = 0;
	if ( check_Vector != null ) {
		check_Vector_size = check_Vector.size();
	}
	__check_filename = null;
	if ( check_Vector_size == 0 ) {
		Message.printStatus ( 2, routine,
		"No data check comments were generated - data are OK or checks "
		+ "have not been implemented for component." );
		// Add some generic comments...
		check_Vector.add ( "" );
		check_Vector.add (
		"No potential output issues were detected." );
		check_Vector.add ( "HOWEVER, DATA CHECKS HAVE ONLY BEEN "
		+ "IMPLEMENTED FOR WELL STATIONS AND WELL RIGHTS." );
		check_Vector.add (
		"Data checks are only performed when products are saved " +
		"with write*() commands -" );
		check_Vector.add (
		"be sure to save output to cause checks to run." );
		check_Vector.add (
		"Refer to the log file for the data processing transaction " +
		"record." );
	}

	// Check vector should now always be non-zero size...

	// Write the check results to a file that has the same name
	// as the commands file but with a ".chk" extension...
	PrintWriter out = null;
	__check_filename = __gui.getCommandsFilename();
	if ( (__check_filename == null) || __check_filename.equals("")){
		// Use a default check file name...
		__check_filename = "StateDMI.chk";
	}
	else {	__check_filename = __check_filename + ".chk";
	}
	try {
	// TODO SAM 2006-04-12
	// Probably want to list to user using relative path.
	//__check_filename = IOUtil.getPathUsingWorkingDir(
		//__check_filename);
	out = new PrintWriter ( new FileWriter(__check_filename) );
	out.println ( "#" );
	out.println ( "# " + __check_filename + " - " +
		IOUtil.getProgramName() + " check file" );
	out.println ( "#" );
	IOUtil.printCreatorHeader ( out, "#", 80, 0 );
	String [] hydrobase_comments = null;
	if ( __hdmi != null ) {
		hydrobase_comments = __hdmi.getVersionComments();
	}
	if ( hydrobase_comments != null ) {
		for ( int i = 0; i < hydrobase_comments.length; i++ ) {
			out.println ( "# " + hydrobase_comments[i] );
		}
	}
	int csize = 0;
	if ( check_Vector != null ) {
		csize = check_Vector.size();
	}
	for ( int i = 0; i < csize; i++ ) {
		out.println ( (String)check_Vector.get(i) );
	}
	out.close();
	addToResultsFileList ( __check_filename );
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, routine,
		"Unable to create check file \"" +
		__check_filename + "\"." );
	}
	**/

	if ( __data_check_count == 0 ) {
		Message.printStatus ( 2, routine,
		"No data check comments were generated - data are OK or checks "
		+ "have not been implemented for component.\n"	+
		"No potential output issues were detected.\n" +
		"Refer to the log file for more information");

	}

	if ( __command_warning_Vector.size() > 0) {
		// Old style user information, which will be phased out as the
		// data check file is phased in...
		message = "There were warnings generated during processing.\n" +
		"See the log file for information.\n" +
		"The output may be incomplete.";
		
		runSuccessful = false;
		
		//if ( (check_Vector != null) && (check_Vector.size() > 0) ) {
		if( __data_check_count > 0) {
		// New style user information...
			message +=
			"\n  \nReview the data check file " +
			"for a summary of possible data issues.";
		}
		Message.printWarning (1, routine, message );
	}
	else if( __data_check_count > 0) {
	//else if ( (check_Vector != null) && (check_Vector.size() > 0) ) {
		// New style user information...
		message =
		"Processing has been successful with no major warnings." +
		"\n  \n" +
		"Review the data check file for a summary of possible data issues.";

		if(!(IOUtil.isBatch()))
			new ResponseJDialog ( __gui, "Run Successful!", message, ResponseJDialog.OK);

		Message.printStatus (2, routine, message );
	}
	if ( update_count > 0 ) {
		Message.printWarning (1, routine,
		"There were warnings printed for obsolete commands.\n" +
		"See the log file for information.  The output may be incomplete.");
	}
	}
	Message.setPropValue ("WarningDialogViewLogButton=false");

	// Clean up...
	message = null;
}

/**
Check for obsolete commands and print an appropriate message.  Handling of warning increments is done in the
calling code.
@param command_String Command as string to check.
@param message_tag Message tag for logging.
@param i_for_message Command number for messages (1+).
*/
private boolean processCommands_CheckForObsoleteCommands( String command_String, String message_tag, int i_for_message)
{	String routine = getClass().getName() + ".processCommands_CheckForObsoleteCommands";
	// Check for obsolete commands (e.g., from old makenet,
	// watright, and demandts DMI programs).  Do this at the end
	// because this logic may seldom be hit if valid commands are
	// processed above.  Print at level 1 because these messages
	// need to be addressed.
	
	// StateDMI commands...
	
	boolean is_obsolete = false;
	if (command_String.regionMatches(true, 0,"setIrrigationPracticeTSFromHydroBase", 0, 36)) {
		// Use the same code as reading the well rights...
		//do_readWellRightsFromHydroBase ( command_tag, command,
		//StateDMI.APP_TYPE_STATECU,
		//StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY );
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The SetIrritationPracticeTSFromHydroBase() command is obsolete.  Use ReadIrrigationPracticeTSFromHydroBase()." );
	}
	else if (command_String.regionMatches(true,0,"synchronizeIrrigationPracticeAndCropPatternTS",0,45)) {
		//do_synchronizeIrrigationPracticeAndCropPatternTS (
		//	command_tag, command );
		is_obsolete = true;
		Message.printWarning ( 1,formatMessageTag(message_tag,i_for_message), routine,
		"The SynchronizeIrritationPracticeAndCropPatternTS() command is obsolete." );
	}
	else if (command_String.regionMatches(true, 0, "setIrrigationPracticeTSMaxPumpingToRights", 0, 41)) {
		// Use the StateMod well rights component so components
		// within the data set are used as flags...
		/*do_limitTSToRights ( command_tag, command,
			StateDMI.APP_TYPE_STATECU,
			StateCU_DataSet.
			COMP_IRRIGATION_PRACTICE_TS_YEARLY );
			*/
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The SetIrrigationPracticeTSMaxPumpingToRights() command is obsolete.  Use SetIrrigationPracticePumpingMaxUsingWellRights()." );
	}
	else if (command_String.regionMatches(true, 0, "setIrrigationPracticeTSSprinklerAreaFromList", 0, 44)){
		// Use the same code as reading the well rights...
		//do_readWellRightsFromHydroBase ( command_tag, command,
		//StateDMI.APP_TYPE_STATECU,
		//StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY );
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The SetIrrigationPracticeTSSprinklerAreaFromList() command is obsolete.  Use SetIrrigationPracticeTSSprinklerAcreageFromList()." );
	}

	// Makenet commands...

	else if (command_String.regionMatches(true, 0, "-auto_label", 0, 11)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -auto_label Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0, "-fancydesc", 0, 10)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -fancydesc Makenet command is obsolete.  " +
		"Use the NameFormat parameter when filling station data from HydroBase." );
	}
	else if (command_String.regionMatches(true, 0, "-label_ap", 0, 9)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -label_ap Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0, "labelcarrier", 0, 12)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The labelcarrier() Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"-label_commonid",0,15)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -label_commonid Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"-label_name",0,11)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -label_name Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"-label_netid",0,12)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -label_netid Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"-label_pf",0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -label_pf Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"-label_riverid",0,14)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -label_riverid Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"-label_water",0,12)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -label_water Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"-nodb",0,5)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -nodb Makenet command is obsolete.  The network editor can create output independent of " +
		"HydroBase." );
	}
	else if (command_String.regionMatches(true, 0,"-nofiles",0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -nofiles Makenet command is obsolete.  The network editor can create output independent of " +
		"model files." );
	}
	else if (command_String.regionMatches(true, 0,"-noplot",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -noplot Makenet command is obsolete.  The network editor can create a plot when requested.");
	}
	else if (command_String.regionMatches(true, 0,"-norivershading",0,14)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -norivershading Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"page=",0,5)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The page= Makenet command is obsolete.  Use the network editor properties." );
	}
	else if (command_String.regionMatches(true, 0,"setprfbase",0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setprfbase() Makenet command is obsolete.  Use the SetStreamEstimateCoefficients() command." );
	}
	else if (command_String.regionMatches(true, 0,"setprfgage",0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setprfgage() Makenet command is obsolete.  Use the SetStreamEstimateCoefficients() command." );
	}
	else if (command_String.regionMatches(true, 0,"setprfgain",0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setprfgain() Makenet command is obsolete.  Use the SetStreamEstimateCoefficients() command." );
	}
	// Put after longer versions...
	else if (command_String.regionMatches(true, 0,"setprf",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setprf() Makenet command is obsolete.  Use the SetStreamEstimateCoefficients() command." );
	}
	else if (command_String.regionMatches(true, 0,"setris",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setrin() Makenet command is obsolete.  Use the FillRiverNetwork*() commands." );
	}
	else if (command_String.regionMatches(true, 0,"setris",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setris() Makenet command is obsolete.  Use the SetStreamGageStation() command." );
	}

	// Watright commands...

	else if (command_String.regionMatches(true, 0, "-aggregate_well_rights", 0, 22)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -aggregate_well_rights Watright command is obsolete.  Define locations as aggregates." );
	}
	else if (command_String.regionMatches(true, 0, "aggres", 0, 6)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The aggres() Watright command is obsolete.  Use the SetReservoirAggregate*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-area_best", 0, 10)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -area_best Watright command is obsolete.  See the FillDiversionStationsFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "-area_best2", 0, 11)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -area_best2 Watright command is obsolete.  See the FillDiversionStationsFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "-area_best3", 0, 11)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -area_best3 Watright command is obsolete.  See the FillDiversionStationsFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "-area_comments",0,14)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -area_comments Watright command is obsolete.  See the FillDiversionStationsFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "-area_gis",0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -area_gis Watright command is obsolete.  See the FillDiversionStationsFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "-area_tia",0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -area_tia Watright command is obsolete.  See the FillDiversionStationsFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "-basin",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -basin Watright command is obsolete.  Use StateView to generate a list of rights." );
	}
	else if (command_String.regionMatches(true, 0, "basin=",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The basin= Watright command is obsolete.  Use StateView to generate a list of rights." );
	}
	else if (command_String.regionMatches(true, 0, "-depletions",0,11)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -depletions Watright command is obsolete.  Use the SetWellStationDepletionTablesFromRTN() command." );
	}
	else if (command_String.regionMatches(true, 0, "-divsystem_area_from_first", 0, 26)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -divsystem_area_from_first Watright command is obsolete.  See the SetDiversionMultiStruct() command." );
	}
	else if (command_String.regionMatches(true, 0, "ignore",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1,formatMessageTag(message_tag,i_for_message), routine,
		"The ignore() Watright command is obsolete.  Command parameters allow ignoring diversions, as appropriate." );
	}
	else if (command_String.regionMatches(true, 0, "-makertn",0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -makertn Watright command is obsolete.  There is currently no replacement in StateDMI." );
	}
	else if (command_String.regionMatches(true, 0, "-mindec",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -mindec Watright command is obsolete.  Use the DecreeMin parameter with commands that " +
		"read rights from HydroBase." );
	}
	else if (command_String.regionMatches(true, 0, "-netcond",0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -netcond Watright command is obsolete.  Use StateView to view net conditional rights." );
	}
	else if (command_String.regionMatches(true, 0, "-noheader",0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -noheader Watright command is obsolete.  Use StateView to view water right reports." );
	}
	else if (command_String.regionMatches(true, 0, "-old_admin_num_format",0,21)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -old_admin_num_format Watright command is obsolete.  Administration numbers are now always " +
		"right-justified." );
	}
	else if (command_String.regionMatches(true, 0, "-oldisf",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -oldisf Watright command is obsolete.  Instream flow rights are formatted to current standards." );
	}
	else if (command_String.regionMatches(true, 0, "-ostatemod",0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -ostatemod Watright command is obsolete.  Use the WriteDiversionRightsToStateMod() command, " +
		"or similar." );
	}
	else if (command_String.regionMatches(true, 0, "rin=",0,4)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The rin= Watright command is obsolete.  Use the ReadDiversionStationsFromNetwork() command, " +
		"or similar." );
	}
	else if (command_String.regionMatches(true, 0, "-rin",0,4)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -rin Watright command is obsolete.  Use the ReadDiversionStationsFromNetwork() command, " +
		"or similar." );
	}
	else if (command_String.regionMatches(true, 0, "rtn=",0,4)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The rtn= Watright command is obsolete.  Use the SetDiversionStationDelayTablesFromRTN() command." );
	}
	else if (command_String.regionMatches(true, 0, "-salpha",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -salpha Watright command is obsolete.  Use StateView to generate a stream alpha report." );
	}
	else if (command_String.regionMatches(true, 0, "setdiv",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setdiv() Watright command is obsolete.  Use the SetDiversionStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setdivr",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setdivr() Watright command is obsolete.  Use the SetDiversionRight() command." );
	}
	else if (command_String.regionMatches(true, 0, "-set_idvcom",0,11)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -set_idvcom Watright command is obsolete.  Use the SetDiversionStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "-set_idvcomw",0,12)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -set_idvcomw Watright command is obsolete.  Use the SetWellStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setifa",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setifa() Watright command is obsolete.  Use the SetInstreamFlowDemandTS*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "setifahdr",0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setifahdr() Watright command is obsolete.  Use the SetInstreamFlowDemandTS*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "setifmhdr",0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setifmhdr() Watright command is obsolete.  Use the SetInstreamFlowDemandTS*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "setisf",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setisf() Watright command is obsolete.  Use the SetInstreamFlowStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setisfr",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setisfr() Watright command is obsolete.  Use the SetInstreamFlowRight() command." );
	}
	else if (command_String.regionMatches(true, 0, "setres",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setres() Watright command is obsolete.  Use the SetReservoirStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setresacs",0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setresacs() Watright command is obsolete.  Use the SetReservoirStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setresevap",0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setresevap() Watright command is obsolete.  Use the SetReservoirStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setrespool",0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setrespool() Watright command is obsolete.  Use the SetReservoirStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setresprecip",0,12)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setresprecip() Watright command is obsolete.  Use the SetReservoirStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setresr",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setisfr() Watright command is obsolete.  Use the SetReservoirRight() command." );
	}
	else if (command_String.regionMatches(true, 0, "-set_right_switch_to_year",0,25)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -set_right_switch_to_year Watright command is obsolete.  " +
		"See the ReadWellRightsFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "setwell1",0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setwell1() Watright command is obsolete.  Use the SetWellStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setwell2",0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setwell2() Watright command is obsolete.  Use the SetWellStation() command." );
	}
	else if (command_String.regionMatches(true, 0, "setwellr",0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setwellr() Watright command is obsolete.  Use the SetWellRight() command." );
	}
	else if (command_String.regionMatches(true, 0, "-slist",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -slist Watright command is obsolete.  Use commands that read list files." );
	}
	else if (command_String.regionMatches(true, 0, "-sort",0,5)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -sort Watright command is obsolete.  Sort water rights in StateView reports." );
	}
	else if (command_String.regionMatches(true, 0, "-taba",0,5)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -taba Watright command is obsolete.  Use StateView to generate a tabulation report." );
	}
	else if (command_String.regionMatches(true, 0, "-tabr",0,5)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -tabr Watright command is obsolete.  Use StateView to generate a tabulation report." );
	}
	else if (command_String.regionMatches(true,0,"-treat_div_as_dw",0,16)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -treat_div_as_dw Watright command is obsolete.  No similar feature is enabled in StateDMI." );
	}
	else if (command_String.regionMatches(true, 0, "-wd",0,3)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wd Watright command is obsolete.  Use StateView to generate a list of rights." );
	}
	else if (command_String.regionMatches(true, 0, "-wellrtn",0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wellrtn Watright command is obsolete.  Use the SetWellStationDelayTablesFromRTN() command." );
	}
	else if (command_String.regionMatches(true, 0, "wrclass",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The wrclass() Watright command is obsolete.  See the Read*RightsFromHydroBase() commands." );
	}
	else if (command_String.regionMatches(true, 0,"-wrclass_average_admin_num",0,26)){
		is_obsolete = true;
		Message.printWarning ( 1,formatMessageTag(message_tag,i_for_message), routine,
		"The -wrclass_average_admin_num Watright command is obsolete.  See the Read*RightsFromHydroBase() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-wrclass_average_app_date",0,25)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wrclass_average_app_date Watright command is obsolete.  " +
		"See the Read*RightsFromHydroBase() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-wrclass_get_app_date_then_average",0,34)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wrclass_get_app_date_then_average Watright command is obsolete.  " +
		"See the Read*RightsFromHydroBase() commands." );
	}

	// Demandts commands...

	else if (command_String.regionMatches(true, 0, "-aggdemands", 0, 11)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -aggdemands Demandts command is obsolete.  Use the SetDiversionAggregate*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-crops", 0, 6)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -crops Demandts command is obsolete.  Use the StateCU Crop Pattern Time Series commands." );
	}
	else if (command_String.regionMatches(true, 0, "-crops_list", 0, 11)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -crops_list Demandts command is obsolete.  Use StateView or TSTool to list crop type data." );
	}
	else if (command_String.regionMatches(true, 0, "-crops_old", 0, 10)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -crops_old Demandts command is obsolete.  Crop numbers are no longer used." );
	}
	else if (command_String.regionMatches(true, 0, "-cropyear", 0, 9)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -cropsyear Demandts command is obsolete.  All years are typically processed or see the Year" +
		" parameter for some commands." );
	}
	else if (command_String.regionMatches(true, 0, "-cy", 0, 3)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -cy Demandts command is obsolete.  Use the SetOuputYearType() command." );
	}
	else if (command_String.regionMatches(true, 0, "-demands", 0, 8)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -demands Demandts command is obsolete.  Use StateMod Diversion Demand Time Series commands." );
	}
	else if (command_String.regionMatches(true, 0, "-diversions", 0, 11)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -diversions Demandts command is obsolete.  Use StateMod Diversion Historical Time Series commands." );
	}
	else if (command_String.regionMatches(true, 0, "-eff1", 0, 5)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -eff1 Demandts command is obsolete.  Use SetDiversionStation() with annual average " +
		"efficiency, as appropriate." );
	}
	else if (command_String.regionMatches(true, 0, "-eff12", 0, 6)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -eff12 Demandts command is obsolete.  Average monthly efficiencies are the default." );
	}
	else if (command_String.regionMatches(true, 0, "-eff_always_enforce_limits", 0, 26)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -eff_always_enforce_limits Demandts command is obsolete.  "+
		"See the CalculateDiversionStationEfficiencies() command." );
	}
	else if (command_String.regionMatches(true, 0,"-eff_create_stm",0,15)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -eff_create_stm Demandts command is obsolete.  "+
		"See the efficiency report file for calculated efficiencies." );
	}
	else if (command_String.regionMatches(true, 0, "-effhigh", 0, 8)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -effhigh Demandts command is obsolete.  "+
		"See the CalculateDiversionStationEfficiencies() command." );
	}
	else if (command_String.regionMatches(true, 0, "-eff_ignore_le_zero", 0, 19)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -eff_ignore_le_zero Demandts command is obsolete.  "+
		"See the CalculateDiversionStationEfficiencies() command." );
	}
	else if (command_String.regionMatches(true, 0, "-efflow", 0, 7)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -efflow Demandts command is obsolete.  See the CalculateDiversionStationEfficiencies() command." );
	}
	else if (command_String.regionMatches(true, 0, "-effold", 0, 7)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -effold Demandts command is obsolete.  Efficiencies are only updated with specific commands,"+
		" as documented." );
	}
	else if (command_String.regionMatches(true, 0, "-effperiod", 0, 10)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -effperiod Demandts command is obsolete.  See the CalculateDiversionStationEfficiencies() command." );
	}
	else if (command_String.regionMatches(true, 0, "-effrd", 0, 6)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -effrd Demandts command is obsolete.  Calculated efficiencies are rounded to the nearest " +
		"1% by default." );
	}
	else if (command_String.regionMatches(true, 0, "-fillall", 0, 8)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -fillall Demandts command is obsolete.  Use the FillDiversion*TS*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-filldata", 0, 9)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -filldata Demandts command is obsolete.  Use the ReadPatternFile() command." );
	}
	else if (command_String.regionMatches(true, 0, "-fillinitzero", 0,13)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -fillinitzero Demandts command is obsolete.  Use the FillDiversion*TS*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-fillmonave", 0,11)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -fillmonave Demandts command is obsolete.  Use the FillDiversion*TS*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "fillPattern", 0,11)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The fillPattern() Demandts command is obsolete.  Use the FillDiversion*TSMonthlyPattern() commands." );
	}
	else if (command_String.regionMatches(true, 0,"-fillusingcomments", 0,18)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -fillusingcomments Demandts command is obsolete.  "
		+"Use the UseDiversionComments=true parameter with the "
		+ "ReadDiversionHistoricalTSMonthlyFromHydroBase() command." );
	}
	else if (command_String.regionMatches(true, 0, "-fillzero", 0,9)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -fillzero Demandts command is obsolete.  Use the FillDiversion*TS*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-fwapprodate", 0,12)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -fwapprodate Demandts command is obsolete.  "+
		"Use the LimitDiversion*TSMonthlyToRights() commands.");
	}
	else if (command_String.regionMatches(true, 0, "-isfstatemod", 0,12)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -isfstatemod Demandts command is obsolete.  "+
		"Use the ReadInstreamFlowStationsFromStateMod() command.");
	}
	else if (command_String.regionMatches(true, 0, "-istatemod", 0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -istatemod Demandts command is obsolete.  Use the "
		+ "ReadDiversionHistoricalTSMonthlyFromStateMod() command.");
	}
	else if (command_String.regionMatches(true, 0, "-icu", 0,4)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -icu Demandts command is obsolete.  Use the " +
		"ReadIrrigationWaterRequirementTSMonthlyFromStateCU() command.");
	}
	else if (command_String.regionMatches(true, 0, "multistruct", 0,11)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -multistruct() Demandts command is obsolete.  Use the SetDiversionMultiStruct*() commands.");
	}
	else if (command_String.regionMatches(true, 0, "-nogenesis", 0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -nogenesis Demandts command is obsolete.  No replacement functionality has been implemented.");
	}
	else if (command_String.regionMatches(true, 0, "outtsfilename", 0,13)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -outtsfilename Demandts command is obsolete.  Use the WriteDiversion*TSMonthlyToStateMod() commands.");
	}
	else if (command_String.regionMatches(true, 0, "-rephigh", 0, 8)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -rephigh Demandts command is obsolete.  See the CalculateDiversionStationEfficiencies() command." );
	}
	else if (command_String.regionMatches(true, 0, "-rights", 0, 7)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -rights Demandts command is obsolete.  See the ReadDiversionRightsFromStateMod() command." );
	}
	else if (command_String.regionMatches(true, 0, "rightsexempt", 0,12)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -rightsexempt Demandts command is obsolete.  See the LimitDiversion*TSMonthlyToRights() commands.");
	}
	else if (command_String.regionMatches(true, 0, "-rstatemod", 0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -rstatemod Demandts command is obsolete.  Use the ReadReservoirStationsFromStateMod() command.");
	}
	else if (command_String.regionMatches(true, 0, "setconstantbefore", 0,17)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setconstantbefore() Demandts command is obsolete.  Use the SetDiversion*TSMonthlyConstant() commands.");
	}
	else if (command_String.regionMatches(true, 0, "setcrops", 0,8)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The setcrops() Demandts command is obsolete.  Use the SetIrrigationPracticeTS() command.");
	}
	else if (command_String.regionMatches(true, 0, "seteff", 0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The seteff() Demandts command is obsolete.  Use the SetDiversionStation() and SetWellStation() commands.");
	}
	else if (command_String.regionMatches(true, 0, "-sstatemod", 0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -sstatemod Demandts command is obsolete.  Use the ReadDiversionStationsFromStateMod() command.");
	}
	else if (command_String.regionMatches(true, 0, "-useaggcap", 0,10)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -useaggcap Demandts command is obsolete.  Use the SetDiversionStationCapacitiesFromTS() command.");
	}
	else if (command_String.regionMatches(true, 0, "-usehistcap", 0,11)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -usehistcap Demandts command is obsolete.  Use the setDiversionStationCapacitiesFromTS() command.");
	}
	else if (command_String.regionMatches(true, 0, "-wells_calculated", 0, 17)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wells_calculated Demandts command is obsolete.  Use StateMod Well Demand Time Series commands." );
	}
	else if (command_String.regionMatches(true, 0,"-wells_historic",0,15)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wells_historic Demandts command is obsolete.  Use StateMod Well Historical Time Series commands." );
	}
	else if (command_String.regionMatches(true, 0, "-wells_ignore_DIV",0,17)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wells_ignore_DIV Demandts command is obsolete.  No replacement for this functionality exists." );
	}
	else if (command_String.regionMatches(true,0,"-wells_ignore_DW",0,16)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wells_ignore_DW Demandts command is obsolete.  No replacement for this functionality exists." );
	}
	else if (command_String.regionMatches(true, 0, "-wells_ignore_WEL",0,17)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wells_ignore_WEL Demandts command is obsolete.  No replacement for this functionality exists." );
	}
	else if (command_String.regionMatches(true, 0, "-wy", 0, 3)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -wy Demandts command is obsolete.  Use the SetOuputYearType() command." );
	}

	// Shared among old DMIs...

	else if (command_String.regionMatches(true, 0, "-basename", 0, 9)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -basename command is obsolete.  File names are explicitly specified in commands." );
	}
	else if (command_String.regionMatches(true, 0, "-cropyear", 0, 9)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -cropyear command is obsolete.  Commands have parameters to specify the crop year." );
	}
	else if (command_String.regionMatches(true, 0, "-datasource", 0, 11)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -datasource command is obsolete.  Select the HydroBase database at login." );
	}
	else if (command_String.regionMatches(true, 0, "-dbhost", 0, 7)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -dbhost command is obsolete.  Select the HydroBase database at login." );
	}
	else if (command_String.regionMatches(true, 0, "-d", 0, 2)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -d command is obsolete.  Use the SetDebugLevel() command." );
	}
	else if (command_String.regionMatches(true, 0, "divsystem", 0, 9)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The divsystem() command is obsolete.  Use the SetDiversionSystem*() commands." );
	}
	else if (command_String.regionMatches(true, 0, "-h", 0, 2)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -h command is obsolete.  Use StateDMI -h on the command line or use the Help About menu." );
	}
	else if (command_String.regionMatches(true, 0, "-informix", 0, 9)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -informix command is obsolete.  Select the HydroBase database at login." );
	}
	else if (command_String.regionMatches(true, 0, "-no_daily_data",0,14)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -no_daily_data command is obsolete.  Commands have parameters to control daily data in output." );
	}
	else if (command_String.regionMatches(true, 0,"-nolog",0,6)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -nolog command is obsolete.  A log file is always created." );
	}
	else if (command_String.regionMatches(true, 0, "-no_well_data",0,13)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -no_well_data command is obsolete.  Commands have parameters to control well data in output." );
	}
	else if (command_String.regionMatches(true, 0, "replace",0,7)){
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The replace() command is obsolete.  Use the set*TS() commands to specify replacement time series.");
	}
	else if (command_String.regionMatches(true, 0, "-sqlserver", 0, 10)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -sqlserver command is obsolete.  Select the HydroBase database at login." );
	}
	else if (command_String.regionMatches(true, 0, "-v", 0, 2)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -v command is obsolete.  Use StateDMI -v on the command line or use the Help About menu." );
	}
	else if (command_String.regionMatches(true, 0, "-w", 0, 2)) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The -w command is obsolete.  Use the SetWarningLevel() command." );
	}
	// Date of format MM/YYYY used to specify the start and end period in the old DMIs...
	else if ( (command_String.indexOf("/") > 0) &&
		StringUtil.isInteger(StringUtil.getToken(command_String,"/",0,0)) &&
		StringUtil.isInteger(StringUtil.getToken(command_String,"/",0,1)) ) {
		is_obsolete = true;
		Message.printWarning ( 1, formatMessageTag(message_tag,i_for_message), routine,
		"The MM/YYYY command is obsolete.  Use the SetOutputPeriod() command." );
	}
	return is_obsolete;
}

// TODO - SAM 2004-04-10 Need to enable this but it is not so much of a
// priority since people seem to be using the GUI - see TSTool for how to
// enable
/**
Process a StateDMI commands file.  This routine parses the commands file
and calls processCommands with the information found in that file.
This is only called when running in batch mode or from the GUI when running
a commands file without reading into the GUI.
@param dmi Database connection.
@param commandFile File name containing tstool commands.
@param is_batch Indicates whether a batch mode is run.  True means no GUI.
False means either a full (main GUI) or partial (plots only) GUI.
*/
/*
public void processCommands (/ *HBDMI dmi,* / String filename, boolean is_batch)
throws Exception
{	// TODO
	// originally had a dmi passed in
	String message, routine = "StateDMI_Processor.processCommands";
	IOUtil.isBatch (is_batch);
	__workingDir = IOUtil.getProgramWorkingDir();
	String iline;
	BufferedReader in = null;
	Vector cmdVec = null;
	try {
		in = new BufferedReader (new FileReader ( filename));
		cmdVec = new Vector (10, 10);
	}
	catch (Exception e) {
		message = "Error opening commands file \"" + filename + "\"";
		Message.printWarning (1, routine, message);
		throw new Exception (message);
	}

	// add each line to the vector of commands
	//
	while ((iline = in.readLine()) != null) {
		try {
		// try around each line...
		// If empty line, skip...

		if (iline.trim().length() == 0) {
			// Blank line.  Do not add to list...
			continue;
		}

		// first remove any () around the time series, if they exist
		if (iline.startsWith("(") && iline.endsWith(")")) {
			iline = iline.substring(1, iline.length()-1);
		}

		cmdVec.add (iline);
		} // end try
		catch (Exception e) {
			Message.printWarning (1, routine,
			"Error processing command \"" + iline + "\"");
		}
	}

	try {	processCommands (/ * dmi, * / cmdVec);
	} catch (Exception e) {
		message = "Error processing command strings.";
		Message.printWarning (1, routine, message);
		Message.printWarning (2, routine, e);
		throw new Exception (message);
	}
}
*/

/**
Reset all the data/results vectors to be empty.
@param appendResults if false, remove all the previous results before processing.
*/
private void processCommands_ResetDataForRunStart ( boolean appendResults )
throws Exception
{
	setPropContents ( "OutputStart", null );
	setPropContents ( "OutputEnd", null );
	setPropContents ( "OutputYearType", YearType.CALENDAR );
	// Clear all old results
	if ( !appendResults ) {
		clearResults();
	}
	// Clear the HydroBase DMI caches
	HydroBaseDMI dmi = getHydroBaseDMIConnection();
	if ( dmi != null ) {
		dmi.clearCaches();
	}

	// Create data sets, to track which components are created and store
	// data check information by component.
	// TODO SAM 2006-04-10
	// This may allow the list of output files to be removed at some point.
	// However, there are output files (e.g., efficiency reports) that are
	// not currently data components.  Some things that could use further study:
	// 1)	Should the data set components be used instead of the separate Vectors?
	// 2)	Should the data sets be integrated with the StateDMI_JFrame so
	//	that results can be taken from the data set?

	// Need the following to register data with
	// StateMod and StateCU to allow validation cross-checks between components.
	__StateCU_DataSet = new StateCU_DataSet();
	__StateMod_DataSet = new StateMod_DataSet();
	////// StateMod data //////
	// Well Components
	DataSetComponent comp = __StateMod_DataSet.getComponentForComponentType( StateMod_DataSet.COMP_WELL_STATIONS );
	comp.setData ( __SMWellList );
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_WELL_RIGHTS );
	comp.setData ( __SMWellRightList );
	// Diversion Components
	comp = __StateMod_DataSet.getComponentForComponentType( StateMod_DataSet.COMP_DIVERSION_STATIONS );
	comp.setData ( __SMDiversionStationList );
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_DIVERSION_RIGHTS );
	comp.setData ( __SMDiversionRightList );
	// Operational Components
	comp = __StateMod_DataSet.getComponentForComponentType( StateMod_DataSet.COMP_OPERATION_RIGHTS);
	comp.setData ( __SMOperationalRightList );
	// Plan Components
	comp = __StateMod_DataSet.getComponentForComponentType( StateMod_DataSet.COMP_PLANS );
	comp.setData ( __SMPlanList );
	// Stream Gage Components
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_STREAMGAGE_STATIONS);
	comp.setData ( __SMStreamGageStationList );
//		// Delay Table Monthly
//		comp = __StateMod_DataSet.getComponentForComponentType (
//			StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY);
//		comp.setData ( __SMDelayTableMonthly_Vector );
//		// Delay Table Daily
//		comp = __StateMod_DataSet.getComponentForComponentType (
//			StateMod_DataSet.COMP_DELAY_TABLES_DAILY);
//		comp.setData ( __SMDelayTableDaily_Vector );
	// Reservoir Components
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_RESERVOIR_STATIONS);
	comp.setData ( __SMReservoirStationList );
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_RESERVOIR_RIGHTS);
	comp.setData ( __SMReservoirRightList );
	// Instream Flow Components
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_INSTREAM_STATIONS);
	comp.setData ( __SMInstreamFlowStationList );
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_INSTREAM_RIGHTS);
	comp.setData ( __SMInstreamFlowRightList );
	// Stream Estimate Components
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS);
	comp.setData ( __SMStreamEstimateStationList );
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS);
	comp.setData ( __SMStreamEstimateCoefficients_Vector );
	// River Network Components
	comp = __StateMod_DataSet.getComponentForComponentType ( StateMod_DataSet.COMP_RIVER_NETWORK);
	comp.setData ( __SMRiverNetworkNode_Vector );
	
	////// StateCU data //////
	// TODO KAT 2007-04-16
	// Need to add checks for Time Series data components
	// Crop Pattern TS Data
//		comp = __StateCU_DataSet.getComponentForComponentType(
//			StateCU_DataSet.COMP_CROP_PATTERN_TS_YEARLY );
//		comp.setData ( __CUCropPatternTS_Vector );
//		// Irrigation Practice TS Yearly Data
//		comp = __StateCU_DataSet.getComponentForComponentType(
//			StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY );
//		comp.setData ( __CUIrrigationPracticeTS_Vector );
	// Blaney-Criddle Data
	comp = __StateCU_DataSet.getComponentForComponentType( StateCU_DataSet.COMP_BLANEY_CRIDDLE );
	comp.setData ( __CUBlaneyCriddle_Vector );
	// Penman-Monteith Data
	comp = __StateCU_DataSet.getComponentForComponentType( StateCU_DataSet.COMP_PENMAN_MONTEITH );
	comp.setData ( __CUPenmanMonteith_Vector );
	// Climate Station Data
	comp = __StateCU_DataSet.getComponentForComponentType( StateCU_DataSet.COMP_CLIMATE_STATIONS );
	comp.setData ( __CUClimateStation_Vector );
	// Crop Characteristics Data
	comp = __StateCU_DataSet.getComponentForComponentType( StateCU_DataSet.COMP_CROP_CHARACTERISTICS );
	comp.setData ( __CUCropCharacteristics_Vector );
	// Location Data
	comp = __StateCU_DataSet.getComponentForComponentType( StateCU_DataSet.COMP_CU_LOCATIONS );
	comp.setData ( __CULocation_Vector );
}

/**
Process a request, required by the CommandProcessor interface.
This is a generalized way to allow commands to call specialized functionality
through the interface without directly naming a processor.  For example, the
request may involve data that only the StateDMI_Processor has access to and that a command does not.
Currently the following requests are handled:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Request</b></td>	<td><b>Description</b></td>
</tr>

<tr>
<td><b>AddCommandProcessorEventListener</b></td>
<td>Add a CommandProcessorEventListener to the processor, which will pass on events from
commands to these listeners.  It is expected that the listener will be added before each
run (via commands) and will be removed at the end of the run.  This design may need to
change as testing occurs.  Parameters to this request are:
<ol>
<li>    <b>TS</b> Monthly time series to process, as TS (MonthTS) object.</li>
<li>    <b>Index</b> The index (0+) of the time series identifier being processed,
        as an Integer.</li>
</ol>
Returned values from this request are:
<ol>
<li>    <b>CommandProcessorEventListener</b>the listener to add.</li>
</ol>
</td>
</tr>

<tr>
<td><b>RunCommands</b></td>
<td>Run commands to create the results:
<ol>
<li>	<b>CommandList</b> A Vector of Command instances to run.</li>
<li>	<b>InitialWorkingDir</b> The initial working directory as a String, to initialize paths.</li>
</ol>
Returned values from this request are:
<ol>
<li>	None - time series results will contain the results.</li>
</ol>
</td>
</tr>

</table>
@param request_params An optional list of parameters to be used in the request.
@exception Exception if the request cannot be processed.
@return the results of a request, or null if a value is not found.
*/
public CommandProcessorRequestResultsBean processRequest ( String request, PropList request_params )
throws Exception
{	// Call helper methods based on the request that is being made...
    if ( request.equalsIgnoreCase("AddCommandProcessorEventListener") ) {
        return processRequest_AddCommandProcessorEventListener ( request, request_params );
    }
    else if ( request.equalsIgnoreCase("GetWorkingDirForCommand") ) {
		return processRequest_GetWorkingDirForCommand ( request, request_params );
	}
	else if ( request.equalsIgnoreCase("RunCommands") ) {
		return processRequest_RunCommands ( request, request_params );
	}
	else if ( request.equalsIgnoreCase("SetHydroBaseDMI") ) {
		return processRequest_SetHydroBaseDMI ( request, request_params );
	}
	else {
		StateDMIProcessorRequestResultsBean bean = new StateDMIProcessorRequestResultsBean();
		String warning = "Unknown StateDMIProcessor request \"" + request + "\"";
		bean.setWarningText( warning );
		// TODO SAM 2007-02-07 Need to figure out a way to indicate
		// an error and pass back useful information.
		throw new UnrecognizedRequestException ( warning );
	}
}

/**
Process the AddCommandProcessorEventListener request.
*/
private CommandProcessorRequestResultsBean processRequest_AddCommandProcessorEventListener (
        String request, PropList request_params )
throws Exception
{   StateDMIProcessorRequestResultsBean bean = new StateDMIProcessorRequestResultsBean();
    // Get the necessary parameters...
    Object o = request_params.getContents ( "CommandProcessorEventListener" );
    if ( o == null ) {
            String warning = "Request AddCommandProcessorEventListener() does not " +
            		"provide a CommandProcessorEventListener parameter.";
            bean.setWarningText ( warning );
            bean.setWarningRecommendationText ( "This is likely a software code error.");
            throw new RequestParameterNotFoundException ( warning );
    }
    CommandProcessorEventListener listener = (CommandProcessorEventListener)o;
    addCommandProcessorEventListener ( listener );
    // No data are returned in the bean.
    return bean;
}

/**
Process the GetWorkingDirForCommand request.  This runs a processor on only the SetWorkingDir() commands
in a command list.  The initial working directory is set to that of the processor.
If no SetWorkingDir() commands are found, then the current initial working directory will be returned.
*/
private CommandProcessorRequestResultsBean processRequest_GetWorkingDirForCommand (
		String request, PropList request_params )
throws Exception
{	StateDMIProcessorRequestResultsBean bean = new StateDMIProcessorRequestResultsBean();
	// Get the necessary parameters...
	Object o = request_params.getContents ( "Command" );
	if ( o == null ) {
			String warning = "Request GetWorkingDirForCommand() does not provide a Command parameter.";
			bean.setWarningText ( warning );
			bean.setWarningRecommendationText (
					"This is likely a software code error.");
			throw new RequestParameterNotFoundException ( warning );
	}
	Command command = (Command)o;
	// Get the index of the requested command...
	int index = indexOf ( command );
	// Get the setWorkingDir() commands...
	List neededCommandsList = new Vector();
	neededCommandsList.add ( "SetWorkingDir" );
	List setWorkingDir_CommandVector = StateDMI_Processor_Util.getCommandsBeforeIndex (
			index,
			this,
			neededCommandsList,
			false );	// Get all, not just last
	String WorkingDir = getInitialWorkingDir();
	if ( neededCommandsList.size() > 0 ) {
		// Have some SetWorkingDir() commands so need to do some more work
		// Create a local command processor
		StateDMI_Processor statedmi_processor = new StateDMI_Processor();
		statedmi_processor.setInitialWorkingDir ( getInitialWorkingDir() );
		int size = setWorkingDir_CommandVector.size();
		// Add all the commands (currently no method to add all because this is normally not done).
		for ( int i = 0; i < size; i++ ) {
			statedmi_processor.addCommand ( (Command)setWorkingDir_CommandVector.get(i));
		}
		// Run the commands to set the working directory in the temporary processor...
		try {
			statedmi_processor.runCommands(
				null,	// Process all commands in this processor
				null );	// No need for controlling properties since controlled by commands
			WorkingDir = (String)statedmi_processor.getPropContents ( "WorkingDir");
		}
		catch ( Exception e ) {
			// This is a software problem.
			String routine = getClass().getName() + ".processRequest_GetWorkingDirForCommand";
			Message.printWarning(2, routine, "Error getting working directory for command." );
			Message.printWarning(2, routine, e);
			// Rethrow
			throw e;
		}
	}
	// Return the working directory as a String.  This can then be used in editors, for
	// example.  The WorkingDir property will have been set in the temporary processor.
	PropList results = bean.getResultsPropList();
	results.set( "WorkingDir", WorkingDir );
	return bean;
}

/**
Process the RunCommands request.
*/
private CommandProcessorRequestResultsBean processRequest_RunCommands (
		String request, PropList request_params )
throws Exception
{	StateDMIProcessorRequestResultsBean bean = new StateDMIProcessorRequestResultsBean();
	// Get the necessary parameters...
	// Command list.
	Object o = request_params.getContents ( "CommandList" );
	if ( o == null ) {
			String warning = "Request RunCommands() does not provide a CommandList parameter.";
			bean.setWarningText ( warning );
			bean.setWarningRecommendationText (	"This is likely a software code error.");
			throw new RequestParameterNotFoundException ( warning );
	}
	List commands = (List)o;
	// Whether commands should create output...
	Object o3 = request_params.getContents ( "CreateOutput" );
	if ( o3 == null ) {
			String warning = "Request RunCommands() does not provide a CreateOutput parameter.";
			bean.setWarningText ( warning );
			bean.setWarningRecommendationText ( "This is likely a software code error.");
			throw new RequestParameterNotFoundException ( warning );
	}
	Boolean CreateOutput_Boolean = (Boolean)o3;
	// Set properties as per the legacy application.
	PropList props = new PropList ( "RunCommands");
	props.set ( "CreateOutput", "" + CreateOutput_Boolean );

	runCommands ( commands, props );
	// No results need to be returned.
	return bean;
}

/**
Process the SetHydroBaseDMI request.
*/
private CommandProcessorRequestResultsBean processRequest_SetHydroBaseDMI (
		String request, PropList request_params )
throws Exception
{	StateDMIProcessorRequestResultsBean bean = new StateDMIProcessorRequestResultsBean();
	// Get the necessary parameters...
	Object o = request_params.getContents ( "HydroBaseDMI" );
	if ( o == null ) {
			String warning = "Request SetHydroBaseDMI() does not provide a HydroBaseDMI parameter.";
			bean.setWarningText ( warning );
			bean.setWarningRecommendationText ( "This is likely a software code error.");
			throw new RequestParameterNotFoundException ( warning );
	}
	HydroBaseDMI dmi = (HydroBaseDMI)o;
	// Add an open HydroBaseDMI instance, closing a previous connection
	// of the same name if it exists.
	__hdmi = dmi;
	// No results need to be returned.
	return bean;
}

/**
Read the command file and initialize new commands.
@param path Path to the command file - this should be an absolute path.
@param createUnknownCommandIfNotRecognized If true, create a GenericCommand
if the command is not recognized.  This is being used during transition of old
string commands to full Command classes.
@param append If true, the commands will be appended to the existing commands.
@exception IOException if there is a problem reading the file.
@exception FileNotFoundException if the specified commands file does not exist.
*/
public void readCommandFile ( String path, boolean createUnknownCommandIfNotRecognized, boolean append )
throws IOException, FileNotFoundException
{	String routine = getClass().getName() + ".readCommandFile";
	BufferedReader br = null;
	br = new BufferedReader( new FileReader(path) );
	setCommandFileName ( path );   // This is used in headers, etc.
	// Set the working directory because this may be used by other commands.
	File path_File = new File(path);
	setInitialWorkingDir ( path_File.getParent() );
	String line;
	Command command = null;
	StateDMICommandFactory cf = new StateDMICommandFactory();
	// Use this to control whether listeners should be notified for each
	// insert.  Why would this be done?  If, for example, a GUI should display
	// the progress in reading/initializing the commands.
	//
	// Why would this not be done?  Becuse of performance issues.
	boolean notifyListenersForEachAdd = true;
	// If not appending, remove all...
	if ( !append ) {
		removeAllCommands();
	}
	// Now process each line in the file and turn into a command...
	int numAdded = 0;
	while ( true ) {
		line = br.readLine();
		if ( line == null ) {
			break;
		}
		// Trim spaces from the end of the line to clean up file.
		line = line.trim();
		// Create a command from the line.
		// Normally will create the command even if not recognized.
		if ( createUnknownCommandIfNotRecognized ) {
			try {
				command = cf.newCommand ( line, createUnknownCommandIfNotRecognized );
			}
			catch ( UnknownCommandException e ) {
				// Should not happen because of parameter passed above
			}
		}
		else {
			try {
				command = cf.newCommand ( line, createUnknownCommandIfNotRecognized );
			}
			catch ( UnknownCommandException e ) {
				// TODO SAM 2007-09-08 Evaluate how to handle unknown commands at load without stopping the load
				// In this case skip the command, although the above case may always be needed?
			}
		}
		// Have a command instance.  Initialize the command (parse the command string) and check its arguments.
		String fixme = "FIXME! ";  // String for inserted messages
		try {
			command.initializeCommand(
				line,	// Command string, needed to do full parse on parameters
				this,	// Processor, needed to make requests
				true);	// Do full initialization (parse)
		}
		catch ( InvalidCommandSyntaxException e ) {
		    // Can't use cf.newCommand() because it will recognized the command
		    // and generate yet another exception!  So, treat as a generic command with a problem.
		    Message.printWarning (2, routine, "Invalid command syntax.  Adding command with problems:  " + line );
            Message.printWarning(3, routine, e);
            // CommandStatus will be set while initializing so no need to set here
            // Do it anyway to make sure something does not fall through the cracks
            if ( (command != null) && (command instanceof CommandStatusProvider) ) {
                CommandStatus status = ((CommandStatusProvider)command).getCommandStatus();
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                "Invalid command syntax (" + e + ").",
                                "Correct the command.  See log file for details." ) );
            }
            // Add generic commands as comments prior to this command to show the original,
            Command command2 = new GenericCommand ();
            command2.setCommandString ( "#" + fixme +
                    "The following command had errors and needs to be corrected below and this comment removed.");
            CommandStatus status = ((CommandStatusProvider)command2).getCommandStatus();
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            "There was an error loading the following command.",
                            "Correct the command below (typically a parameter error due to manual edit)." ) );
            addCommand ( command2, notifyListenersForEachAdd );
            ++numAdded;
            command2 = new GenericCommand ();
            command2.setCommandString ( "#" + fixme + line );
            status = ((CommandStatusProvider)command2).getCommandStatus();
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            "There was an error loading this command.",
                            "Correct the command below (typically a parameter error due to manual edit)." ) );
            addCommand ( command2, notifyListenersForEachAdd );
            ++numAdded;
            // Allow the bad command to be loaded below.  It may have no arguments or partial
            // parameters that need corrected.
		}
		catch ( InvalidCommandParameterException e) {
            // Can't use cf.newCommand() because it will recognized the command
            // and generate yet another exception!  So, treat as a generic command with a problem.
		    Message.printWarning (2, routine, "Invalid command parameter.  Adding command with problems:  " + line );
            Message.printWarning(3, routine, e);
            // CommandStatus will be set while initializing so no need to set here
            // Do it anyway to make sure something does not fall through the cracks
            if ( (command != null) && (command instanceof CommandStatusProvider) ) {
                CommandStatus status = ((CommandStatusProvider)command).getCommandStatus();
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                "Invalid command parameter." + e + ").",
                                "Correct the command.  See log file for details." ) );
            }
            // Add generic commands as comments prior to this command to show the original,
            Command command2 = new GenericCommand ();
            command2.setCommandString ( "# " + fixme +
                    "The following command had errors and needs to be corrected below and this comment removed.");
            CommandStatus status = ((CommandStatusProvider)command2).getCommandStatus();
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            "There was an error loading the following command.",
                            "Correct the command below (typically an error due to manual edit)." ) );
            addCommand ( command2, notifyListenersForEachAdd );
            ++numAdded;
            command2 = new GenericCommand ();
            command2.setCommandString ( "#" + line );
            status = ((CommandStatusProvider)command2).getCommandStatus();
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            "There was an error loading this command.",
                            "Correct the command below (typically an error due to manual edit)." ) );
            addCommand ( command2, notifyListenersForEachAdd );
            ++numAdded;
            // Allow the bad command to be loaded below.  It may have no arguments or partial
            // parameters that need corrected.
		}
        catch ( Exception e ) {
            // TODO SAM 2007-11-29 Need to decide whether to handle here or in command with CommandStatus
            // It is important that the command get added, even if it is invalid, so the user can edit the
            // command file.  They will likely need to replace the command, not edit it.
            Message.printWarning( 1, routine, "Unexpected error creating command \"" + line + "\" - report to software support." );
            Message.printWarning ( 3, routine, e );
            // CommandStatus likely not set while initializing so need to set here to alert user
            if ( (command != null) && (command instanceof CommandStatusProvider) ) {
                CommandStatus status = ((CommandStatusProvider)command).getCommandStatus();
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                "Unexpected error creating the command.",
                                "Check the command syntax.  See log file for details." ) );
            }
            // Add generic commands as comments prior to this command to show the original,
            Command command2 = new GenericCommand ();
            command2.setCommandString ( "#" + fixme +
                    " The following command had errors and needs to be corrected below and this comment removed.");
            CommandStatus status = ((CommandStatusProvider)command2).getCommandStatus();
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            "There was an error loading the following command.",
                            "Correct the command below (typically an error due to manual edit)." ) );
            addCommand ( command2, notifyListenersForEachAdd );
            ++numAdded;
            command2 = new GenericCommand ();
            command2.setCommandString ( "#" + fixme + line );
            status = ((CommandStatusProvider)command2).getCommandStatus();
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            "There was an error loading this command.",
                            "Correct the command below (typically an error due to manual edit)." ) );
            addCommand ( command2, notifyListenersForEachAdd );
            ++numAdded;
            // Allow the bad command to be loaded below.  It may have no arguments or partial
            // parameters that need corrected.
        }
        // TODO SAM 2007-10-09 Evaluate whether to call listeners each time a command is added.
        // Could be good to indicate progress of load in the GUI.
        // For now, add the command, without notifying listeners of changes...
        if ( command != null ) {
            // Check the command parameters
            String command_tag = "" + numAdded + 1;  // Command number, for messaging
            int error_count = 0;
            try {  
                command.checkCommandParameters(command.getCommandParameters(), command_tag, 2 );
            }
            catch ( InvalidCommandParameterException e ) {
                /* TODO SAM 2008-05-14 Evaluate whether this can work - don't want a bunch
                of extra comments for commands that are already being flagged with status.
                // Add generic commands as comments prior to this command to show the original,
                Command command2 = new GenericCommand ();
                command2.setCommandString ( "#" + fixme +
                "The following command had errors and needs to be corrected below and this comment removed.");
                CommandStatus status = ((CommandStatusProvider)command2).getCommandStatus();
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                "There was an error loading the following command.",
                                "Correct the command below (typically an error due to manual edit)." ) );
                addCommand ( command2, notify_listeners_for_each_add );
                ++num_added;
                command2 = new GenericCommand ();
                command2.setCommandString ( "#" + fixme + line );
                status = ((CommandStatusProvider)command2).getCommandStatus();
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                "There was an error loading this command.",
                                "Correct the command below (typically an error due to manual edit)." ) );
                addCommand ( command2, notify_listeners_for_each_add );
                ++num_added;
                */
                // Add command status to the command itself, handling whether a recognized
                // command or a generic command (string command)...
                String message = "Error loading command - invalid syntax (" + e + ").";
                if ( command instanceof CommandStatusProvider ) {
                       if ( CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command).
                               greaterThan(CommandStatusType.UNKNOWN) ) {
                           // No need to print a message to the screen because a visual marker will be shown, but log...
                           Message.printWarning ( 2,
                                   MessageUtil.formatMessageTag(command_tag,
                                           ++error_count), routine, message );
                       }
                       if ( command instanceof GenericCommand ) {
                            // The command class will not have added a log record so do it here...
                            ((CommandStatusProvider)command).getCommandStatus().addToLog ( CommandPhaseType.RUN,
                                    new CommandLogRecord(CommandStatusType.FAILURE,
                                            message, "Check the log for more details." ) );
                       }
                }
                else {
                    // Command has not been updated to set warning/failure in status so show here
                    Message.printWarning ( 2,
                        MessageUtil.formatMessageTag(command_tag,
                        ++error_count), routine, message );
                }
                // Log the exception.
                if (Message.isDebugOn) {
                    Message.printDebug(3, routine, e);
                }
            }
            // Now finally add the command to the list
            addCommand ( command, notifyListenersForEachAdd );
            ++numAdded;
            // Run discovery on the command so that the identifiers are available to other commands.
            // Do up front and then only when commands are edited.
            if ( command instanceof CommandDiscoverable ) {
                readCommandFile_RunDiscoveryOnCommand ( command );
            }
        }
	} // Looping over commands in file
	// Close the file...
	br.close();
	// Now notify listeners about the add one time (only need to do if it
	// was not getting done for each add...
	if ( !notifyListenersForEachAdd ) {
		notifyCommandListListenersOfAdd ( 0, (numAdded - 1) );
	}
}

/**
Run discovery on the command. This will, for example, make available a list of time series
to be requested with the ObjectListProvider.getObjectList() method.
*/
private void readCommandFile_RunDiscoveryOnCommand ( Command command_read )
{   String routine = getClass().getName() + ".commandList_EditCommand_RunDiscovery";
    // Run the discovery...
    Message.printStatus(2, routine, "Running discovery mode on command:  \"" + command_read + "\"" );
    try {
        ((CommandDiscoverable)command_read).runCommandDiscovery(indexOf(command_read));
    }
    catch ( Exception e )
    {
        // For now ignore because edit-time input may not be complete...
        String message = "Unable to make discover run - may be OK if partial data.";
        Message.printStatus(2, routine, message);
    }
}

/**
Read supplemental HydroBase_StructureIrrigSummaryTS (new is
HydroBase_StructureView) records.  This does not
actually read the records but retrieves them from memory in
StateDMI_HydroBase_ParcelUseTS objects - the records are
defined in the setCropPatternTS() and setCropPatternTSFromList() commands.
If a supplemental record is available that conflicts with existing data,
the supplemental data will be used and a warning is printed.
@param crop_patterns Vector of HydroBase_StructureIrrigSummaryTS (e.g., as read
from HydroBase) (new is HydroBase_StructureView).  This Vector will be added to and returned.
@param wdid_list List of WDIDs to be checked.  Each string is parsed into WD
and ID parts.  It is assumed that only valid WDIDs are passed - any errors
parsing are ignored (should not happen).
@param InputStart_DateTime The starting date to process data.
@param InputEnd_DateTime The ending date to process data.
*/
protected List readSupplementalStructureIrrigSummaryTSListForWDIDList ( List crop_patterns, List wdid_list,
	DateTime InputStart_DateTime, DateTime InputEnd_DateTime,
	List HydroBase_Supplemental_StructureIrrigSummaryTS_Vector,
	CommandStatus status, String command_tag, int warningLevel, int warning_count )
{	String routine = "StateDMI_Processor.readSupplementalStructureIrrigSummaryTSListForWDIDList";
	if ( crop_patterns == null ) {
		crop_patterns = new Vector();
	}
	int cpsize = crop_patterns.size();
	StateDMI_HydroBase_StructureView sits = null; // Supplemental
	HydroBase_StructureView sits2 = null; // From HydroBase
	// Get a list of integer WDIDs to process...
	Message.printStatus ( 2, routine, "Getting supplemental acreage data "+
		"from setCropPatternTS() commands for:  " + wdid_list );
	int nwdid_list = 0;
	if ( wdid_list != null ) {
		nwdid_list = wdid_list.size();
	}
	int sits_wd, sits_id; // The WDID parts for the "sits" object
	int iwdid; // For looping through WDIDs.
	boolean found = false; // Used when searching for matching HydroBase and supplemental records.
	// Size of all supplemental data...
	int size=HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.size();
	for ( int i = 0; i < size; i++ ) {
		sits = (StateDMI_HydroBase_StructureView)HydroBase_Supplemental_StructureIrrigSummaryTS_Vector.get(i);
		// Check to see if the record is in the desired year...
		if ( (InputStart_DateTime != null) && (InputEnd_DateTime != null)
			&& ((sits.getCal_year() < InputStart_DateTime.getYear()) ||
			((sits.getCal_year() > InputEnd_DateTime.getYear())) ) ){
			// The data record year is outside the requested year.
			continue;
		}
		// Figure out if the record matches a requested location identifier...
		sits_wd = sits.getWD();
		sits_id = sits.getID();
		for ( iwdid = 0; iwdid < nwdid_list; iwdid++ ) {
			// Now do the lookup on the more generic string ID...
			if ( !((String)wdid_list.get(iwdid)).equalsIgnoreCase(sits.getLocationID())) {
				// Not a match...
				continue;
			}
			// If here, a match was found.  First see if there is
			// an existing matching record in the full data set.
			found = false;
			// TODO SAM 2004-05-18 - this is a major dog.  Need to rework the loops so that the large
			// vector is only traversed once.
			// FIXME SAM 2007-05-14 Need to decide with the State whether this should be flagged as an error.
			for ( int i2 = 0; i2 < cpsize; i2++ ) {
				sits2 = (HydroBase_StructureView)crop_patterns.get(i2);
				if ( (sits2.getWD() == sits_wd) && (sits2.getID() == sits_id) &&
					(sits2.getCal_year() ==	sits.getCal_year()) &&
					sits2.getLand_use().equalsIgnoreCase(sits.getLand_use()) ) {
					// Matching record, replace the old and print a warning...
					String message = "WD " + sits_wd + " ID " + sits_id +
					" supplemental data from SetCropPatternTS() matches raw (HydroBase) crop " +
					"pattern data for " + sits.getCal_year() + " " + sits.getLand_use();
					Message.printWarning ( warningLevel, 
				        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( CommandPhaseType.RUN,
			            new CommandLogRecord(CommandStatusType.WARNING,
			                message, "Using data from set command and overriding HydroBase - " +
			                	"verify the set command." ) );
					found = true;
					// This is OK whether previously processed or not, since it is not additive.
					crop_patterns.set ( i2, sits );
				}
			}
			if ( !found ) {
				// No matching record was found so just add.  Only add if not previously processed.
				if ( sits.hasBeenProcessed() ) {
					// This is a warning that is handled internally but should
					// probably be handled by the modeler.
					String message = "Location " + sits.getLocationID() +
						" supplemental acreage data from SetCropPatternTS(): year=" +
						sits.getCal_year() + " crop=" + sits.getLand_use() + " acres=" +
						StringUtil.formatString(sits.getAcres_total(),"%.3f") +
						" has already been processed once and is " +
						"not being added again.";
					Message.printWarning ( warningLevel, 
				        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( CommandPhaseType.RUN,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Verify that set commands only specify the data once." ) );
				}
				else {
					// Has not been previously processed so add the data...
					Message.printStatus ( 2, routine, "Location " + sits.getLocationID() +
						" supplemental acreage data from SetCropPatternTS(): year=" +
						sits.getCal_year() + " crop=" + sits.getLand_use() + " acres=" +
						StringUtil.formatString(sits.getAcres_total(),"%.3f") );
					crop_patterns.add ( sits );
				}
			}
		}
	}
	return crop_patterns;
}

/**
Method for TSSupplier interface.
Read a time series given a time series identifier string.  The string may be
a file name if the time series are stored in files, or may be a true identifier
string if the time series is stored in a database.  The specified period is
read.  The data are converted to the requested units.
@param tsident_string Time series identifier or file name to read.
@param req_date1 First date to query.  If specified as null the entire period
will be read.
@param req_date2 Last date to query.  If specified as null the entire period
will be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return Time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public TS readTimeSeries (	String tsident_string,
				DateTime req_date1, DateTime req_date2,
				String req_units,
				boolean read_data )
throws Exception
{	Message.printStatus ( 1, "", "Reading \"" + tsident_string + "\"" );

	// For now just create a dummy time series...

	MonthTS ts = new MonthTS();
	DateTime date1 = DateTime.parse ( "1950-01" );
	DateTime date2 = DateTime.parse ( "2000-12" );
	ts.setDate1 ( date1 );
	ts.setDate1Original ( date1 );
	TSIdent id = new TSIdent ( "xxxx..Streamflow.1Day" );
	ts.setDataType ( "Streamflow" );
	ts.setDate2 ( date2 );
	ts.setDate2Original ( date2 );
	ts.allocateDataSpace ();
	ts.setIdentifier ( id );
	for (	DateTime date = new DateTime(date1);
		date.lessThanOrEqualTo(date2);
		date.addDay ( 1 ) ) {
		ts.setDataValue ( date, date.toDouble() );
	}
	Message.printStatus ( 1, "", "Returning default monthly time series" );
	ts.formatOutput( new PropList ("x") );
	return ts;

/* SAMX - need to fill out functionality
	int size = 0;
	if ( _tslist != null ) {
		size = _tslist.size();
	}

	if ( size != 0 ) {
		TS ts = null;
		//  First try the aliases...
		for ( int i = 0; i < size; i++ ) {
			ts = (TS)_tslist.get(i);
			if ( ts == null ) {
				continue;
			}
			if ( ts.getAlias().equalsIgnoreCase(tsident_string) ) {
				return ts;
			}
		}

		// Now try the TSIDs...

		for ( int i = 0; i < size; i++ ) {
			ts = (TS)_tslist.get(i);
			if ( ts == null ) {
				continue;
			}
			//Message.printStatus ( 1, "",
			//"Checking tsid \"" + ts.getIdentifier() + "\"" );
			if ( ts.getIdentifier().equals(tsident_string) ) {
				return ts;
			}
		}
	}

	// If not found, try reading from a persistent source.  If called with
	// an alias, this will fail.  If called with a TSID, this should
	// succeed...

	// HBDMIUtil will read all sources except NWSRFS, NWSCard, RiverWare,
	// and UsgsNwis...
	TS ts = null;
	// Get the input value if specified...
	if ( tsident_string == null ) {
		return null;
	}
	Vector v = StringUtil.breakStringList ( tsident_string, "~", 0 );
	String tsident_string2;	// Version without the input...
	String input_name = null;
	String input_type = null;
	tsident_string2 = (String)v.get(0);
	if ( v.size() == 2 ) {
		input_type = (String)v.get(1);
	}
	else if ( v.size() == 3 ) {
		input_type = (String)v.get(1);
		input_name = (String)v.get(2);
	}
	TSIdent tsident = new TSIdent ( tsident_string2 );
	String source = tsident.getSource();
	//DateTime date1 = null;
	//DateTime date2 = null;
	//if ( req_date1 != null ) {
	//	date1 = req_date1.getDateTime();
	//}
	//if ( req_date2 != null ) {
	//	date2 = req_date2.getDateTime();
	//}

	// Always check the new style first...

	if (	(input_type != null) &&
		input_type.equalsIgnoreCase("DateValue") ) {
		// Read a DateValue time series.  The following allows the time
		// series TSID to be matched exactly against a file that may
		// have more than one time series.
		Message.printStatus ( 1, "", "Reading time series with id \"" +
			tsident_string2 + "\" input_name \"" + input_name +
			"\"" );
		ts = DateValueTS.readTimeSeries ( tsident_string2, input_name,
			req_date1, req_date2, req_units, read_data );
	}
	else if ((input_type != null) &&
		input_type.equalsIgnoreCase("MODSIM") ) {
		// Read a MODSIM time series.  The following allows the time
		// series TSID to be matched exactly against a file that may
		// have more than one time series.
		Message.printStatus ( 1, "", "Reading time series with id \"" +
			tsident_string2 + "\" input_name \"" + input_name +
			"\"" );
		ts = ModsimTS.readTimeSeries ( tsident_string2, input_name,
			req_date1, req_date2, req_units, read_data );
	}
	else if (((input_type != null) &&
		input_type.equalsIgnoreCase("NWSRFS")) ||
		source.equalsIgnoreCase("NWSRFS") ) {
		// Use the NWSRFS DMI...
		ts = TSEngine.NWSRFSDMI_readTimeSeries (
						tsident_string,
						req_date1,
						req_date2,
						req_units,
						read_data );
	}
	else if ( (input_type != null)&&input_type.equalsIgnoreCase("NWSCard")){
		// Use the full identifier...
		ts = NWSCardTS.readTimeSeries ( tsident_string2, input_name,
			req_date1, req_date2,
			req_units, read_data );
	}
	else if ( source.equalsIgnoreCase("NWSCard") ) {
		// Use the full identifier...
		ts = NWSCardTS.readTimeSeries ( tsident_string,
			req_date1, req_date2,
			req_units, read_data );
	}
	else if ( (input_type != null) &&
		input_type.equalsIgnoreCase("RiversideDB") ) {
		// Read from the RTi database...
	}
	else if ( (input_type != null) &&
		input_type.equalsIgnoreCase("USGSNWIS") ) {
		// Read a USGS NWIS time series.  The following allows the time
		// series TSID to be matched exactly against a file that may
		// have more than one time series (?).
		ts = UsgsNwisTS.readTimeSeries ( tsident_string2, input_name,
			req_date1, req_date2, req_units, read_data );
	}
	else {	// HydroBase or StateMod time series...
		ts = HBDMIUtil.getTimeSeries ( _hbdmi, tsident_string,
			req_date1, req_date2, req_units, read_data );
	}
	return ts;
	*/
}

/**
Method for TSSupplier interface.
Read a time series given an existing time series and a file name.
The specified period is read.
The data are converted to the requested units.
@param req_ts Requested time series to fill.  If null, return a new time series.
If not null, all data are reset, except for the identifier, which is assumed
to have been set in the calling code.  This can be used to query a single
time series from a file that contains multiple time series.
@param fname File name to read.
@param date1 First date to query.  If specified as null the entire period will
be read.
@param date2 Last date to query.  If specified as null the entire period will
be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return Time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public TS readTimeSeries (	TS req_ts, String fname,
				DateTime date1, DateTime date2,
				String req_units,
				boolean read_data )
throws Exception
{	return null;
}

/**
Method for TSSupplier interface.
Read a time series list from a file (this is typically used used where a time
series file can contain one or more time series).
The specified period is
read.  The data are converted to the requested units.
@param fname File to read.
@param date1 First date to query.  If specified as null the entire period will
be read.
@param date2 Last date to query.  If specified as null the entire period will
be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return Vector of time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public List readTimeSeriesList (	String fname,
					DateTime date1, DateTime date2,
					String req_units,
					boolean read_data )
throws Exception
{	return null;
}

/**
Method for TSSupplier interface.
Read a time series list from a file or database using the time series identifier
information as a query pattern.
The specified period is read.  The data are converted to the requested units.
@param tsident A TSIdent instance that indicates which time series to query.
If the identifier parts are empty, they will be ignored in the selection.  If
set to "*", then any time series identifier matching the field will be selected.
If set to a literal string, the identifier field must match exactly to be selected.
@param fname File to read.
@param date1 First date to query.  If specified as null the entire period will be read.
@param date2 Last date to query.  If specified as null the entire period will be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return Vector of time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public List readTimeSeriesList (	TSIdent tsident, String fname,
					DateTime date1, DateTime date2,
					String req_units,
					boolean read_data )
throws Exception {
	return null;
}

/**
Remove all CommandProcessorEventListener.
*/
public void removeAllCommandProcessorEventListeners ( )
{   // Just reset the array to null
    __CommandProcessorEventListener_array = null;
}

/**
Remove all commands.
*/
public void removeAllCommands ()
{	List commandList = getCommands();
	int size = commandList.size();
	if ( size > 0 ) {
		commandList.clear ();
		notifyCommandListListenersOfRemove ( 0, size - 1 );
	}
}

/**
Remove a command at a position.
@param index Position (0+) at which to remove command.
*/
public void removeCommandAt ( int index )
{	String routine = "StateDMI_Processor.removeCommandAt";
	List commandList = getCommands();
	commandList.remove ( index );
	notifyCommandListListenersOfRemove ( index, index );
	Message.printStatus(2, routine, "Remove command object at [" + index + "]" );
}

/**
Remove a CommandListListener.
@param listener CommandListListener to remove.
*/
public void removeCommandListListener ( CommandListListener listener )
{	if ( listener == null ) {
		return;
	}
	if ( __CommandListListener_array != null ) {
		// Loop through and set to null any listeners that match the
		// requested listener...
		int size = __CommandListListener_array.length;
		int count = 0;
		for ( int i = 0; i < size; i++ ) {
			if (	(__CommandListListener_array[i] != null) &&
				(__CommandListListener_array[i] == listener) ) {
				__CommandListListener_array[i] = null;
			}
			else {	++count;
			}
		}
		// Now resize the listener array...
		CommandListListener [] newlisteners =
			new CommandListListener[count];
		count = 0;
		for ( int i = 0; i < size; i++ ) {
			if ( __CommandListListener_array[i] != null ) {
				newlisteners[count++] = __CommandListListener_array[i];
			}
		}
		__CommandListListener_array = newlisteners;
		newlisteners = null;
	}
}

// FIXME SAM 2007-10-18 Remove following code when transitioned to other listeners
/**
Remove a ProcessListener.  The matching object address is removed, even if
it was regestered multiple times.
@param listener ProcessListener to remove.
*/
public void removeProcessListener ( ProcessListener listener )
{	if ( listener == null ) {
		return;
	}
	if ( __listeners != null ) {
		// Loop through and set to null any listeners that match the
		// requested listener...
		int size = __listeners.length;
		int count = 0;
		for ( int i = 0; i < size; i++ ) {
			if (	(__listeners[i] != null) &&
				(__listeners[i] == listener) ) {
				__listeners[i] = null;
			}
			else {	++count;
			}
		}
		// Now resize the listener array...
		ProcessListener [] newlisteners = new ProcessListener[count];
		count = 0;
		for ( int i = 0; i < size; i++ ) {
			if ( __listeners[i] != null ) {
				newlisteners[count++] = __listeners[i];
			}
		}
		__listeners = newlisteners;
		newlisteners = null;
	}
}

/**
Reset a __CU*_match_Vector to empty.
@param matchList List of matching identifiers/names to reset to empty.
*/
protected void resetDataMatches ( List matchList )
{	matchList.clear();
}

/**
Run the processor.  When run as a thread from the StateDMIGUI, this method is
called from the Thread.start() method.
*/
/* FIXME SAM remove when code transition is complete
public void run ()
{	__is_running = true;
	try {	processCommands ();
		if ( __command_warning_Vector.size() == 0 ) {
			// No warnings during the run...
			notifyListenersOfProcessStatus ( STATUS_SUCCESS,
			"Success" );
		}
		else {	// There were some warnings during the run...
			notifyListenersOfProcessStatus ( STATUS_ERROR,
			"Warnings Detected" );
		}
	}
	catch ( Exception e ) {
		notifyListenersOfProcessStatus ( STATUS_ERROR, "Error" );
	}
	__is_running = false;
}
*/

/**
Reset the workflow global properties to defaults, necessary when a command processor is rerun.
*/
private void resetWorkflowProperties ()
throws Exception
{   String routine = getClass().getName() + ".resetWorkflowProperties";
    Message.printStatus(2, routine, "Resetting workflow properties." );
    
    // First clear user-defined properties.
    // FIXME SAM 2008-10-14 Evaluate whether needed like TSTool
    //__property_Hashtable.clear();
    // Now make sure that specific controlling properties are cleared out.
    setPropContents("OutputEnd", null );
    setPropContents("OutputStart", null );
    setPropContents("OutputYearType", YearType.CALENDAR );
}

/**
Run the specified commands.  If no commands are specified, run all that are being managed.
@param commands Vector of Command to process.
@param props Properties to control run.  See full list in TSEngine.processCommands.  This
method only acts on the properties shown below.
<td><b>Property</b></td>    <td><b>Description</b></td>
</tr>

<tr>
<td><b>ResetWorkflowProperties</b></td>
<td>If set to true (default), indicates that global properties like output period should be
reset before running.
</td>
<td>False</td>
</tr>

</table>
*/
public void runCommands ( List commands, PropList props )
throws Exception
{
    // Reset the global workflow properties if requested
    String ResetWorkflowProperties = "True";   // default
    if ( props != null ) {
        String prop = props.getValue ( "ResetWorkflowProperties" );
        if ( (prop != null) && prop.equalsIgnoreCase("False") ) {
            ResetWorkflowProperties = "False";
        }
    }
    if ( ResetWorkflowProperties.equalsIgnoreCase("True")) {
        resetWorkflowProperties();
    }
    // Remove all registered CommandProcessorEventListener, so that listeners don't get added
    // more than once if the processor is rerun.  Currently this will require that an OpenCheckFile()
    // command is always run since it is the only thing that handles events at this time.
    removeAllCommandProcessorEventListeners();
    
    // Now call the method to do the processing.

	processCommands ( commands, props );
	
	// Now finalize the results by processing the check files, if any

	if ( __CommandProcessorEventListener_array != null ) {
    	for ( int i = 0; i < __CommandProcessorEventListener_array.length; i++ ) {
    	    CommandProcessorEventListener listener =
    	        (CommandProcessorEventListener)__CommandProcessorEventListener_array[i];
    	    if ( listener instanceof CheckFileCommandProcessorEventListener ) {
    	        CheckFileCommandProcessorEventListener cflistener = (CheckFileCommandProcessorEventListener)listener;
    	        cflistener.finalizeOutput();
    	    }
    	}
	}
	
    // Remove all registered CommandProcessorEventListener again so that if by chance editing, etc. generates
	// events don't want to deal with...
    removeAllCommandProcessorEventListeners();
}

/**
Run the specified commands.  If no commands are specified, run all that are being managed.
@param commands Vector of Command to process.
@param props Properties to control run, including:  CreateOutput=True|False.
*/
public void runCommands_OLD ( List commands, PropList props )
throws Exception
{
	processCommands_OLD ( commands, props );
}

//FIXME SAM 2008-12-23 Make sure data checks are implemented using better design.
/**
Helper method to run data checks for StateCU for a given component type.
@param Type of StateMod component.
*/
/* TODO SAM 2009-04-27 Evaluate removing code if other check commands work
protected void runStateCUDataCheck( int type )
{
	String fname = getCommandFileName();
	// If there is no commands file then use the component and program name for the file name
	if ( fname == null || fname.length() == 0 ) {
		DataSetComponent comp = __StateCU_DataSet.getComponentForComponentType( type );
		String name = comp.getComponentName();
		if ( name == null ){
			name = "CheckFile";
		}
		fname = name + ".StateDMI";
	}
	//addToResultsFileList( __StateCU_DataSet.runComponentChecks(
	//	type, fname, getCommandsAsString(), getFullProgramHeader() ) );
	__data_check_count++;
}
*/

// FIXME SAM 2008-12-23 Make sure data checks are implemented using better design.
/**
Helper method to run data checks for StateMod for a given component type.
@param Type of StateMod component.
*/
/*
protected void runStateModDataCheck( int type )
{
	String fname = getCommandFileName();
	// If type is RiverNetwork then add the data.
	// For some reason the data wasn't being added correctly like other
	// products so override and add the data here
	if ( type == StateMod_DataSet.COMP_RIVER_NETWORK ) {
		DataSetComponent comp = __StateMod_DataSet.getComponentForComponentType( type );
		comp.setData( __SMRiverNetworkNode_Vector );
	}
	// If there is no commands file then use the component and program name for the file name
	if ( fname == null || fname.length() == 0 ) {
		DataSetComponent comp = __StateMod_DataSet.getComponentForComponentType( type );
		String name = comp.getComponentName();
		if ( name == null ){
			name = "CheckFile";
		}
		fname = name + ".StateDMI";
	}
	// FIXME SAM 2008-12-23 Need to fix data check file.
	//addToResultsFileList( new File(__StateMod_DataSet.runComponentChecks(
	//	type, fname, getCommandsAsString(), getFullProgramHeader() )) );
	__data_check_count++;
}
*/

/**
Request that processing be cancelled.  This sets a flag that is detected in the
processCommands() method.  Processing will be cancelled as soon as the current command
completes its processing.
@param cancel_processing_requested Set to true to cancel processing.
*/
public void setCancelProcessingRequested ( boolean cancel_processing_requested )
{	__cancel_processing_requested = cancel_processing_requested;
}

/**
Set the command list, typically only called from constructor.
@param commandList list of commands for processor.
*/
public void setCommands ( List commandList )
{
	__commandList = commandList;
}

/**
Set the name of the commands file where the commands are saved.
@param filename Name of commands file (should be absolute since
it will be used in output headers).
*/
public void setCommandFileName ( String filename )
{
	__commandFilename = filename;
}

/**
Set whether the processor should create output
@param create_output If true, processing commands will execute commands that
create output (e.g., write*() commands).  If false, these commands will not
be executed, increasing execution speed and keeping results in memory only.
*/
public void setCreateOutput ( boolean create_output )
{	__create_output = create_output;
}

/**
Set the initial working directory for the processor.  This is typically the location
of the commands file, or a temporary directory if the commands have not been saved.
Also set the current working directory by calling setWorkingDir() with the same information.
@param InitialWorkingDir The current working directory.
*/
protected void setInitialWorkingDir ( String InitialWorkingDir )
{	String routine = getClass().getName() + ".setInitialWorkingDir";
	Message.printStatus(2, routine, "Setting the initial working directory to \"" + InitialWorkingDir + "\"" );
	__InitialWorkingDir_String = InitialWorkingDir;
	// Also set the working directory...
	setWorkingDir ( __InitialWorkingDir_String );
}

/**
Indicate whether the processor is running.  This should be set in processCommands()
and can be monitored by code (e.g., GUI) that has behavior that depends on whether
the processor is running.
@param is_running indicates whether the processor is running (processing commands).
*/
private void setIsRunning ( boolean is_running )
{
	__is_running = is_running;
}

/**
Set the data for a named property, required by the CommandProcessor
interface.  See the getPropContents method for a list of properties that are handled.
@param prop Property to set.
@return the named property, or null if a value is not found.
*/
public void setProp ( Prop prop ) throws Exception
{	//String key = prop.getKey();
	/* TODO SAM 2005-05-20 Need to start enabling..
	if ( key.equalsIgnoreCase("TSResultsList") ) {
		__tslist = (Vector)prop.getContents();
		// TODO SAM 2005-05-05 Does anything need to be revisited?
	}
	*/
}

/**
Set the contents for a named property, required by the CommandProcessor
interface.  See the getPropContents method for a list of properties that are handled.
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>
</tr>

<tr>
<td><b>InitialWorkingDir</b></td>
<td>A String containing the path to the initial working directory, from which all
paths are determined.  This is usually the directory to the commands file, or the
startup directory.
</td>
</tr>

<tr>
<td><b>OutputEnd</b></td>
<td>A DateTime containing the end of the processing period.
</td>
</tr>

<tr>
<td><b>OutputStart</b></td>
<td>A DateTime containing the start of the processing period.
</td>
</tr>

<tr>
<td><b>WDIDLength</b></td>
<td>An Integer containing the WDID length to be used as a default when formatting new identifiers.
</td>
</tr>

</table>
*/
public void setPropContents ( String prop, Object contents ) throws Exception
{	if ( prop.equalsIgnoreCase("HydroBaseDMIList") ) {
		// TODO SAM 2005-06-08 Currently only allow one connection...
		List<HydroBaseDMI> v = (List<HydroBaseDMI>)contents;
		__hdmi = v.get(0);
	}
	else if ( prop.equalsIgnoreCase("InitialWorkingDir" ) ) {
		setInitialWorkingDir ( (String)contents );
	}
	else if ( prop.equalsIgnoreCase("OutputEnd" ) ) {
		__OutputEnd_DateTime = (DateTime)contents;
	}
	else if ( prop.equalsIgnoreCase("OutputStart" ) ) {
		__OutputStart_DateTime = (DateTime)contents;
	}
	else if ( prop.equalsIgnoreCase("OutputYearType" ) ) {
		__OutputYearType = (YearType)contents;
	}
	else if ( prop.equalsIgnoreCase("StateCU_IrrigationPracticeTS_List") ) {
		__CUIrrigationPracticeTS_Vector = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateCU_Location_List") ) {
		__CULocation_Vector = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_DiversionDemandTSMonthly_List") ) {
		__SMDemandTSMonthlyList = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_DiversionHistoricalTSMonthly_List") ) {
		__SMDiversionTSMonthlyList = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_Network") ) {
		__SM_network = (StateMod_NodeNetwork)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_PlanReturn_List") ) {
		__SMPlanReturnList = (List<StateMod_ReturnFlow>)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_PlanStation_List") ) {
		__SMPlanList = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_PlanWellAugmentation_List") ) {
		__SMPlanWellAugmentationList = (List<StateMod_Plan_WellAugmentation>)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_RiverNetworkNode_List") ) {
		__SMRiverNetworkNode_Vector = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_WellDemandTSMonthly_List") ) {
		__SMWellDemandTSMonthlyList = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_WellHistoricalPumpingTSMonthly_List") ) {
		__SMWellHistoricalPumpingTSMonthlyList = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateModWellRightList") || prop.equalsIgnoreCase("StateMod_WellRight_List") ) {
		__SMWellRightList = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("StateMod_WellStation_List") ) {
		__SMWellList = (List)contents;
	}
	else if ( prop.equalsIgnoreCase("DefaultWDIDLength") || // Newer
		prop.equalsIgnoreCase("WDIDLength" ) ) { // Older
		__defaultWdidLength = ((Integer)contents).intValue();
	}
    else if ( prop.equalsIgnoreCase("WorkingDir") ) {
        setWorkingDir ( (String)contents );
    }
	else {
		String routine = getClass().getName() + ".setPropContents";
		Message.printWarning ( 3, routine, "Unrecognized property \"" + prop + "\" - not setting.");
		// TODO SAM 2008-12-08 Evaluate use
		// Seems to hang the app, perhaps due to threading?  Also, User can set any property with SetProperty
		//throw new RuntimeException ( "Attempting to set unrecognized property \"" + prop + "\"" );
	}
}

/**
Set the working directory for the processor.  This is typically set by
SetInitialWorkingDir() method when initializing the processor and SetWorkingDir() commands.
@param WorkingDir The current working directory.
*/
public void setWorkingDir ( String WorkingDir )
{
	__WorkingDir_String = WorkingDir;
}

/**
Return the number of commands being managed by this processor.  This
matches the Collection interface, although that is not yet fully implemented.
@return The number of commands being managed by the processor
*/
public int size()
{
	return getCommands().size();
}

/**
Print a warning about key identifier/name matches that have occurred when adding CU locations.
@param command Command that was adding the StateCU_Location (String or Command class).
@param replace If true, an existing instance is replaced if found.  If false,
the original instance is used.  This flag should be consistent with how the StateCU_Location were processed.
@param matchList List of strings containing the key id/name values that have matches.
@param data_type String to use in messages to indentify the data object type (e.g., "CU Locations").
*/
protected void warnAboutDataMatches ( Object command, boolean replace, List matchList, String data_type )
{	int size = matchList.size();

	if (size == 0) {
		return;
	}

	StringBuffer matches = new StringBuffer ( (String)matchList.get(0) );
	String id;
	int maxwidth = 100;
	String nl = System.getProperty ( "line.separator" );
	for (int i = 1; i < size; i++) {
		matches.append ( ", " );
		// Limit to "maxwidth" characters per line...
		id = (String)matchList.get(i);
		// 2 is for the ", "
		if ( (matches.length()%maxwidth + (id.length() + 2)) >= maxwidth) {
			matches.append ( nl );
		}
		matches.append ( id );
	}

	// Warn at level 2 since this is a non-fatal error.  Later may add an
	// option to the read methods to give the choice of some behavior when matches are found...

	if ( replace ) {
		Message.printWarning ( __FYI_warning_level,	"StateDMI_Processor.warnAboutDataMatches",
		"The following " + data_type + " were already in memory and were " +
		"overwritten\nwith new data from the \"" + command + "\" " + "command :\n" + matches.toString() );
	}
	else {
		Message.printWarning ( __FYI_warning_level, "StateDMI_Processor.warnAboutDataMatches",
		"The following " + data_type + " were already in memory and were " +
		"retained\ndespite new data from the \"" + command + "\" " + "command :\n" + matches.toString() );
	}
}

}