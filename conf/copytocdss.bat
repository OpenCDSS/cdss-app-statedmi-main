@echo off
rem Simple batch file to copy distribution files to the current C:\cdss location
rem for a manual build.

SET DEST=C:\CDSS\StateDMI-03.00.00beta\bin

@echo on
copy ..\..\HydroBaseDMI\dist\HydroBaseDMI_142.jar %DEST%
copy ..\..\NWSRFS_DMI\dist\NWSRFS_DMI_142.jar %DEST%
copy ..\..\RiversideDB_DMI\dist\RiversideDB_DMI_142.jar %DEST%
copy ..\..\RTi_Common\dist\RTi_Common_142.jar %DEST%
copy ..\..\SatmonSysDMI\dist\SatmonSysDMI_142.jar %DEST%
copy ..\..\StateDMI\dist\StateDMI_142.jar %DEST%
copy ..\..\StateMod\dist\StateMod_142.jar %DEST%
copy ..\..\TSCommandProcessor\dist\TSCommandProcessor_142.jar %DEST%

SET DEST=
