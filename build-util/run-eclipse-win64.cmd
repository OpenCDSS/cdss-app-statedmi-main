@echo off
rem Configure and run Eclipse 64-bit for Windows for standard StateDMI development environment.
rem - 64-bit is used consistent with TSTool
rem - this script assumes that Eclipse is installed in a specific location and more
rem   locations can be added

rem It is assumed that the development environment follows the standard folder structure, for example:
rem - %USERPROFILE%\cdss-dev\StateDMI\git-repos\cdss-app-statedmi-main

rem Determine the script folder and workspace folder.
rem - %~dp0 has backslash at end
set scriptFolder=%~dp0
set workspaceFolder=%scriptFolder%..\..\..\eclipse-workspace
echo Batch file folder: %scriptFolder%
echo Workspace folder: %workspaceFolder%

rem Run Eclipse
rem - the following specifically sets the VM location, which works fine if developers follow that convention
rem - TODO smalers 2017-03-18 could change this script to fall back to using JAVA_HOME

rem Set the absolute path to Eclipse program
rem - sort with the most recent last so that the newest supported version is run
rem - this assumes that the developer is using the newest version installed in a known location
rem - additional "standard" installation locations can be added for StateDMI developers
set eclipseExe=""
echo Checking for Eclipse in standard locations, oldest supported versions first...
set eclipseTryExe="C:\Program Files\Eclipse\eclipse-java-2019-03\eclipse.exe"
echo Checking %eclipseTryExe%
if exist %eclipseTryExe% set eclipseExe=%eclipseTryExe%
rem The eclipseExe variable already contains surrounding double quotes so don't need to use below
if not exist %eclipseExe% goto noeclipse

rem Also try to find Java 8
rem - this is 64-bit
rem - a symbolic link was defined from jdk8 to the specific version to generalize the location
set javawExe=""
echo Checking for Java in standard locations, oldest supported versions first...
set javawTryExe="c:\Program Files\Java\jdk8\bin\javaw.exe"
echo Checking %javawTryExe%
if exist %javawTryExe% set javawExe=%javawTryExe%
rem The javawExe variable already contains surrounding double quotes so don't need to use below
if not exist %javawExe% goto nojavaw

rem If here, run Eclipse using the executable that is known to exist from above checks
rem - also set the title of the window
rem - since 64-bit, can use a larger maximum memory value
echo Starting Eclipse using %eclipseExe%
rem The -data option must come before -vm, etc.
title Eclipse configured for StateDMI 64-bit development
%eclipseExe% -data %workspaceFolder% -vm %javawExe% -vmargs -Xmx2408M
goto end

:noeclipse
rem Expected Eclipse (eclipse.exe) was not found
echo Eclipse was not found using expected locations.
echo Update the run script to find Eclipse or run a different script.
exit /b 1

:nojavaw
rem Expected Java (javaw.exe) was not found
echo Java was not found using expected locations.
echo Update the run script to find Java or run a different script.
exit /b 1

rem Successfully found Eclipse and Java to run
:end
