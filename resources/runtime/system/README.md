# system #

This folder contains configuration files for StateDMI:

* `CDSS.cfg` - This file is the database configuration file for HydroBase login shown when StateDMI starts.
This configuration file may be replaced by the newer datatores design in the future.
However, currently the older design continues to be used as the default database
configuration for direct HydroBase database queries.

* `DATAUNIT` - This file provides data unit conversions.

* `StateDMI.cfg` - This file provides installer default configuration properties for StateDMI.
To override, edit properties in the file `C:\Users\user\.statedmi\5\system\StateDMI.cfg`,
where the `5` in this example is the StateDMI major version.
The user-specified configuration will override the software install configuration.
