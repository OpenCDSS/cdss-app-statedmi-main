@echo off
rem Configure and run Eclipse Neon 3 32-bit Windows for standard StateDMI development environment.
rem -32-bit is used until resources can be applied to update to 64-bit

rem It is assumed that the development environment follows the standard folder structure, for example:
rem - %USERPROFILE%\cdss-dev\StateDMI\git-repos\cdss-app-statedmi-main

rem Run Eclipse
rem - the following specifically sets the VM location, which works fine if developers follow that convention
rem - TODO smalers 2017-03-18 could change this script to fall back to using JAVA_HOME
rem - The following typically uses sybmolic to point jdk8 to specific installed version
@echo on
"C:\Program Files (x86)\eclipse-java-neon-3-win32\eclipse\eclipse.exe" –vm "C:\Program Files (x86)\Java\jdk8\bin\javaw.exe" -vmargs -Xmx1024M
