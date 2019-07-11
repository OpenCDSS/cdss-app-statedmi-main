# datastores #

This folder contains datastore configuration files that are enabled as part of the software install.

* `HydroBase.cfg` - Datastore for HydroBase database connection.
Note that this is separate from the database connection that is opened when a
database is selected at login.
The HydroBase datastore can be used with commands like ReadTableFromDataStore
whereas the login connection does not support datastore features.

* `HydroBaseWeb.cfg` - Datastore for HydroBase web services.
This datastore is enabled for some commands where web services support queries
similar to database queries.

If the above configurations need to be adjusted, copy to the
`C:\Users\user\.statedmi\5\datastores` folder and edit as needed.
The `5` in this example is the StateDMI major version.
These user datastores will take precedent over install datastores if the
same datastore name is used.
