# bin #

This folder contains the executable software files for StateDMI.
StateDMI is written in Java, and consequently to run StateDMI requires
installing a Java Runtime Environment (JRE).
The `jre_18` folder distributed with StateDMI contains version 8 of Java and in the future this folder may change.
The JRE allows the StateDMI installation to be self-contained to minimize
dependencies on other software on the computer.

StateDMI is started using the [Launch4J](http://launch4j.sourceforge.net/) tool.
Launch4J is used to create the `StateDMI.exe` executable for Windows,
and is configured using the `StateDMI.l4j.ini` file.
The launcher calls the JRE with StateDMI and other software files.
Java uses "jar" (Java archive) files to package software files.
This folder contains various StateDMI jar files in addition to third-party jar file libraries.
The jar files starting with `cdss` correspond to OpenCDSS Git repositories.

After starting StateDMI, use ***Help / About StateDMI / Show Software/System Details***
to view information about the JRE and jar file versions.

See the StateDMI Installation and Configuration Appendix documentation for more information.
